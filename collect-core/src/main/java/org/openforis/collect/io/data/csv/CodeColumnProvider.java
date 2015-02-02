/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CodeAttribute;

/**
 * @author M. Togna
 */
public class CodeColumnProvider extends CompositeAttributeColumnProvider<CodeAttributeDefinition> {

	private static final String ITEM_POSITION_FIELD_NAME = "item_pos";
	private static final String ITEM_POSITION_SUFFIX = "class";
	
	private boolean hasExpandedItems = false;
	private List<CodeListItem> expandedItems = null;
	private List<String> expandedItemsFields = null;
	
	public CodeColumnProvider(CSVExportConfiguration config, CodeAttributeDefinition defn) {
		super(config, defn);
		init();
	}

	@Override
	protected void init() {
		if (getConfig().isCodeAttributeExpanded()) {
			CodeList list = attributeDefinition.getList();
			int levelPosition = attributeDefinition.getLevelPosition();
			CodeListService codeListService = getCodeListService();
			List<CodeListItem> items = codeListService.loadItems(list, levelPosition);
			hasExpandedItems = items.size() <= getConfig().getMaxExpandedCodeAttributeItems();
			expandedItems = hasExpandedItems ? items: null;
			expandedItemsFields = hasExpandedItems ? extractExpandedItemsFields(): null;
		}
		super.init();
	}
	
	@Override
	protected String[] getFieldNames() {
		List<String> result = new ArrayList<String>();
		//code field
		result.add(CodeAttributeDefinition.CODE_FIELD);
		//qualifier field
		CodeList list = attributeDefinition.getList();
		if ( hasQualifiableItems(list) ) {
			result.add(CodeAttributeDefinition.QUALIFIER_FIELD);
		}
		//item position field
		if ( getConfig().isIncludeCodeItemPositionColumn() && ! list.isExternal() ) {
			result.add(ITEM_POSITION_FIELD_NAME);
		}
		if (hasExpandedItems) {
			for (CodeListItem item : expandedItems) {
				result.add(item.getCode());
			}
		}
		return result.toArray(new String[result.size()]);
	}

	@Override
	protected String getFieldHeading(String fieldName) {
		if ( ITEM_POSITION_FIELD_NAME.equals(fieldName) ) {
			return "_" + attributeDefinition.getName() + getConfig().getFieldHeadingSeparator() + ITEM_POSITION_SUFFIX;
		} else {
			return super.getFieldHeading(fieldName);
		}
	}
	
	@Override
	protected String extractValue(Attribute<?, ?> attr, String fieldName) {
		if ( ITEM_POSITION_FIELD_NAME.equals(fieldName) ) {
			CodeListService codeListService = getCodeListService();
			CodeListItem item = codeListService.loadItem((CodeAttribute) attr);
			if ( item == null ) {
				return "";
			} else {
				List<CodeListItem> items = codeListService.loadItems(attributeDefinition.getList(), attributeDefinition.getLevelPosition());
				int position = items.indexOf(item) + 1;
				return Integer.toString(position);
			}
		} else if (hasExpandedItems && expandedItemsFields.contains(fieldName)) {
			CodeListItem item = getCodeListService().loadItem((CodeAttribute) attr);
			return Boolean.valueOf(item != null && fieldName.equals(item.getCode())).toString();
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
	
	private List<String> extractExpandedItemsFields() {
		List<String> fields = new ArrayList<String>(expandedItems.size());
		for (CodeListItem item : expandedItems) {
			fields.add(item.getCode()); 
		}
		return fields;
	}

}
