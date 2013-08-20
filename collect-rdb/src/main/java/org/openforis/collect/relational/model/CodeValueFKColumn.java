/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.path.Path;

/**
 * @author S. Ricci
 *
 */
public class CodeValueFKColumn extends DataColumn {
	
	private String defaultCodeValue;

	CodeValueFKColumn(String name, CodeAttributeDefinition defn, Path relPath, String defaultCodeValue) {
		super(name, Types.BIGINT, "bigint", defn, relPath, null, true);
		this.defaultCodeValue = defaultCodeValue;
	}

	@Override
	public Object extractValue(Node<?> context) {
		NodeDefinition defn = getNodeDefinition();
		if ( defn instanceof CodeAttributeDefinition ) {
			Node<?> valNode = super.extractValueNode(context);
			if ( valNode != null && valNode instanceof CodeAttribute ) {
				return extractValue((CodeAttribute) valNode);
			} else if ( defaultCodeValue != null ) {
				ModelVersion version = context.getRecord().getVersion();
				return getDefaultCodeId(((CodeAttributeDefinition) defn).getList(), version);
			}
		}
		return null;
	}

	protected Object extractValue(CodeAttribute valNode) {
		CodeListItem item = findCodeListItem(valNode);
		if ( item == null ) {
			if ( defaultCodeValue == null ) {
				return null;
			} else {
				String codeValue = getCodeValue(valNode);
				if ( defaultCodeValue.equals(codeValue)) {
					CodeAttributeDefinition definition = valNode.getDefinition();
					CodeList list = definition.getList();
					Record record = valNode.getRecord();
					ModelVersion version = record.getVersion();
					return getDefaultCodeId(list, version);
				} else {
					//code list item not found, invalid code?
					return null;
				}
			}
		} else {
			return item.getId();
		}
	}
	
	protected String getCodeValue(CodeAttribute attr) {
		if ( attr == null ) {
			return null;
		} else {
			Field<String> codeField = attr.getCodeField();
			return codeField.getValue();
		}
	}
	
	protected <T extends CodeListItem> T findCodeListItem(CodeAttribute attr) {
		String code = getCodeValue(attr);
		if ( StringUtils.isBlank(code) ) {
			return null;
		} else {
			CodeListService codeListService = getCodeListService((CollectSurvey) attr.getSurvey());
			T item = codeListService.loadItem(attr);
			return item;
		}
	}

	protected Integer getDefaultCodeId(CodeList list, ModelVersion version) {
		CodeListService codeListService = getCodeListService((CollectSurvey) list.getSurvey());
		CodeListItem defaultCodeItem = codeListService.loadRootItem(list, defaultCodeValue, version);
		return defaultCodeItem == null ? -1: defaultCodeItem.getId();
	}

	protected CodeListService getCodeListService(CollectSurvey survey) {
		SurveyContext context = survey.getContext();
		CodeListService codeListService = context.getCodeListService();
		return codeListService;
	}
}
