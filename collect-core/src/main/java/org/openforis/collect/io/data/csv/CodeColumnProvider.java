/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.io.data.csv.Column.DataType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Node;

/**
 * @author M. Togna
 */
public class CodeColumnProvider extends CompositeAttributeColumnProvider<CodeAttributeDefinition> {

	private static final String ITEM_POSITION_FIELD_NAME = "item_pos";
	private static final String ITEM_POSITION_SUFFIX = "class";
	private static final String ITEM_LABEL_FIELD_NAME = "item_label";
	public static final String ITEM_LABEL_SUFFIX = "label";
	
	private boolean ancestorDef;
	private boolean hasExpandedItems = false;
	private List<CodeListItem> expandedItems = null;
	
	public CodeColumnProvider(CSVDataExportParameters config, CodeAttributeDefinition defn, boolean ancestorDef) {
		super(config, defn);
		this.ancestorDef = ancestorDef;
		init();
	}

	@Override
	protected void init() {
		if (getConfig().isCodeAttributeExpanded()) {
			CodeList list = attributeDefinition.getList();
			CollectSurvey survey = (CollectSurvey) list.getSurvey();
			if (survey.isPredefinedCodeList(list)) {
				hasExpandedItems = false;
				expandedItems = null;
			} else {
				int levelPosition = attributeDefinition.getLevelPosition();
				CodeListService codeListService = getCodeListService();
				List<CodeListItem> items = codeListService.loadItems(list, levelPosition);
				hasExpandedItems = items.size() <= getConfig().getMaxExpandedCodeAttributeItems();
				expandedItems = hasExpandedItems ? items: null;
			}
		}
		super.init();
	}
	
	@Override
	protected String[] getFieldNames() {
		List<String> result = new ArrayList<String>();
		//code field
		result.add(CodeAttributeDefinition.CODE_FIELD);
		if (!ancestorDef) {
			// if the code attribute is an ancestor key attribute,
			// only the code value is necessary; 
			// other columns can be exported with the belonging entity
			
			// qualifier field
			CodeList list = attributeDefinition.getList();
			if ( hasQualifiableItems(list) ) {
				result.add(CodeAttributeDefinition.QUALIFIER_FIELD);
			}
			//item position field
			if ( getConfig().isIncludeCodeItemPositionColumn() && ! list.isExternal() ) {
				result.add(ITEM_POSITION_FIELD_NAME);
			}
			//label field
			if ( getConfig().isIncludeCodeItemLabelColumn() && ! list.isExternal() ) {
				result.add(ITEM_LABEL_FIELD_NAME);
			}
		}
		return result.toArray(new String[result.size()]);
	}

	@Override
	public List<Column> generateColumns() {
		List<Column> columns = super.generateColumns();
		if (hasExpandedItems) {
			columns.addAll(generateExpandedItemsColumns());
		}
		return columns;
	}

	private List<Column> generateExpandedItemsColumns() {
		List<Column> columns = new ArrayList<Column>();
		for (CodeListItem item : expandedItems) {
			String header = ColumnProviders.generateHeadingPrefix(attributeDefinition, config) + getConfig().getFieldHeadingSeparator() + item.getCode();
			Column column = new Column(header);
			if (! columns.contains(column)) {
				columns.add(column);
				if (item.isQualifiable()) {
					columns.add(new Column(header + getConfig().getFieldHeadingSeparator() + CodeAttributeDefinition.QUALIFIER_FIELD));
				}
			}
		}
		return columns;
	}
	
	@Override
	protected Column generateFieldColumn(String fieldName, String suffix) {
		if ( CodeAttributeDefinition.CODE_FIELD.equals(fieldName) ) {
			return new Column(ColumnProviders.generateHeadingPrefix(attributeDefinition, config) + suffix);
		} else if ( ITEM_POSITION_FIELD_NAME.equals(fieldName) ) {
			return new Column("_" + ColumnProviders.generateHeadingPrefix(attributeDefinition, config) + getConfig().getFieldHeadingSeparator() + ITEM_POSITION_SUFFIX + suffix, DataType.INTEGER);
		} else if ( ITEM_LABEL_FIELD_NAME.equals(fieldName) ) {
			return new Column(ColumnProviders.generateHeadingPrefix(attributeDefinition, config) + getConfig().getFieldHeadingSeparator() + ITEM_LABEL_SUFFIX + suffix);
		} else {
			return super.generateFieldColumn(fieldName, suffix);
		}
	}
	
	@Override
	public List<Object> extractValues(Node<?> axis) {
		List<Object> values = super.extractValues(axis);
		if (hasExpandedItems) {
			List<Node<?>> attributes = extractNodes(axis);
			List<String> headings = new ArrayList<String>();
			for (CodeListItem item : expandedItems) {
				String heading = ColumnProviders.generateHeadingPrefix(attributeDefinition, config) + getConfig().getFieldHeadingSeparator() + item.getCode();
				if (! headings.contains(heading)) {
					CodeAttribute attr = findAttributeByCode(attributes, item.getCode());
					values.add(attr != null);
					if (item.isQualifiable()) {
						values.add(attr == null ? null: attr.getValue().getQualifier());
					}
				}
			}
		}
		return values;
	}

	protected CodeAttribute findAttributeByCode(List<Node<?>> attributes, String code) {
		for (Node<?> attr : attributes) {
			CodeAttribute codeAttr = (CodeAttribute) attr;
			Code val = codeAttr.getValue();
			if (val != null && code.equals(val.getCode())) {
				return codeAttr;
			}
		}
		return null;
	}
	
	@Override
	protected Object extractValue(Attribute<?, ?> attr, String fieldName) {
		if ( ITEM_POSITION_FIELD_NAME.equals(fieldName) 
				|| ITEM_LABEL_FIELD_NAME.equals(fieldName) ) {
			CodeListService codeListService = getCodeListService();
			CodeListItem item = codeListService.loadItem((CodeAttribute) attr);
			if ( item == null ) {
				return null;
			} else if ( ITEM_POSITION_FIELD_NAME.equals(fieldName) ) {
				List<CodeListItem> items = codeListService.loadItems(attributeDefinition.getList(), attributeDefinition.getLevelPosition());
				int position = items.indexOf(item) + 1;
				return position;
			} else {
				return item.getLabel(getConfig().getLanguageCode(), true);
			}
		} else {
			return super.extractValue(attr, fieldName);
		}
	}

	private boolean hasQualifiableItems(CodeList list) {
		CodeListService codeListService = getCodeListService();
		return codeListService.hasQualifiableItems(list);
	}
	
	private CodeListService getCodeListService() {
		SurveyContext context = attributeDefinition.getSurvey().getContext();
		CodeListService codeListService = context.getCodeListService();
		return codeListService;
	}
	
}
