package org.openforis.collect.relational.data.internal;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.CodeValueFKColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeValueFKColumnValueExtractor extends DataTableDataColumnValueExtractor<CodeValueFKColumn> {

	public CodeValueFKColumnValueExtractor(DataTable table, CodeValueFKColumn column) {
		super(table, column);
	}
	
	@Override
	public Object extractValue(Node<?> context) {
		CodeAttributeDefinition defn = (CodeAttributeDefinition) column.getNodeDefinition();
		Node<?> valNode = super.extractValueNode(context);
		if ( valNode != null && valNode instanceof CodeAttribute ) {
			return extractValue((CodeAttribute) valNode);
		} else if ( column.getDefaultCodeValue() != null ) {
			ModelVersion version = context.getRecord().getVersion();
			return getDefaultCodeItemId(((CodeAttributeDefinition) defn).getList(), version);
		} else {
			return null;
		}
	}
	
	private Object extractValue(CodeAttribute valNode) {
		CodeListItem item = findCodeListItem(valNode);
		if ( item == null ) {
			String defaultCodeValue = column.getDefaultCodeValue();
			String codeValue = getCodeValue(valNode);
			if ( defaultCodeValue == null || (codeValue != null && ! codeValue.equals(defaultCodeValue)) ) {
				//no default code value specified or code list item not found (invalid code?)
				return null;
			} else {
				CodeAttributeDefinition definition = valNode.getDefinition();
				CodeList list = definition.getList();
				Record record = valNode.getRecord();
				ModelVersion version = record.getVersion();
				return getDefaultCodeItemId(list, version);
			}
		} else {
			return item.getId();
		}
	}
	
	private String getCodeValue(CodeAttribute attr) {
		if ( attr == null ) {
			return null;
		} else {
			Field<String> codeField = attr.getCodeField();
			return codeField.getValue();
		}
	}
	
	private <T extends CodeListItem> T findCodeListItem(CodeAttribute attr) {
		String code = getCodeValue(attr);
		if ( StringUtils.isBlank(code) ) {
			return null;
		} else {
			CodeListService codeListService = getCodeListService((CollectSurvey) attr.getSurvey());
			T item = codeListService.loadItem(attr);
			return item;
		}
	}

	private Integer getDefaultCodeItemId(CodeList list, ModelVersion version) {
		CodeListService codeListService = getCodeListService((CollectSurvey) list.getSurvey());
		CodeListItem defaultCodeItem = codeListService.loadRootItem(list, column.getDefaultCodeValue(), version);
		return defaultCodeItem == null ? CodeTableDataExtractor.DEFAULT_CODE_ROW_ID: defaultCodeItem.getId();
	}

	private CodeListService getCodeListService(CollectSurvey survey) {
		SurveyContext context = survey.getContext();
		CodeListService codeListService = context.getCodeListService();
		return codeListService;
	}
}
