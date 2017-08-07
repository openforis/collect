/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

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
	
	private boolean hasExpandedItems = false;
	private List<CodeListItem> expandedItems = null;
	
	public CodeColumnProvider(CSVExportConfiguration config, CodeAttributeDefinition defn) {
		super(config, defn);
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
		//qualifier field
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
		return result.toArray(new String[result.size()]);
	}

	@Override
	public List<String> generateColumnHeadings() {
		List<String> headings = super.generateColumnHeadings();
		if (hasExpandedItems) {
			headings.addAll(generateExpandedItemsHeadings());
		}
		return headings;
	}

	private List<String> generateExpandedItemsHeadings() {
		List<String> headings = new ArrayList<String>();
		for (CodeListItem item : expandedItems) {
			String heading = ColumnProviders.generateHeadingPrefix(attributeDefinition, config) + getConfig().getFieldHeadingSeparator() + item.getCode();
			headings.add(heading);
			if (item.isQualifiable()) {
				headings.add(heading + getConfig().getFieldHeadingSeparator() + CodeAttributeDefinition.QUALIFIER_FIELD);
			}
		}
		return headings;
	}
	
	@Override
	protected String generateFieldHeading(String fieldName) {
		if ( CodeAttributeDefinition.CODE_FIELD.equals(fieldName) ) {
			return ColumnProviders.generateHeadingPrefix(attributeDefinition, config);
		} else if ( ITEM_POSITION_FIELD_NAME.equals(fieldName) ) {
			return "_" + ColumnProviders.generateHeadingPrefix(attributeDefinition, config) + getConfig().getFieldHeadingSeparator() + ITEM_POSITION_SUFFIX;
		} else if ( ITEM_LABEL_FIELD_NAME.equals(fieldName) ) {
			return ColumnProviders.generateHeadingPrefix(attributeDefinition, config) + getConfig().getFieldHeadingSeparator() + ITEM_LABEL_SUFFIX;
		} else {
			return super.generateFieldHeading(fieldName);
		}
	}
	
	@Override
	public List<String> extractValues(Node<?> axis) {
		List<String> values = super.extractValues(axis);
		if (hasExpandedItems) {
			List<Node<?>> attributes = extractNodes(axis);
			for (CodeListItem item : expandedItems) {
				CodeAttribute attr = findAttributeByCode(attributes, item.getCode());
				values.add(Boolean.valueOf(attr != null).toString());
				if (item.isQualifiable()) {
					values.add(attr == null ? "": attr.getValue().getQualifier());
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
	protected String extractValue(Attribute<?, ?> attr, String fieldName) {
		if ( ITEM_POSITION_FIELD_NAME.equals(fieldName) 
				|| ITEM_LABEL_FIELD_NAME.equals(fieldName) ) {
			CodeListService codeListService = getCodeListService();
			CodeListItem item = codeListService.loadItem((CodeAttribute) attr);
			if ( item == null ) {
				return "";
			} else if ( ITEM_POSITION_FIELD_NAME.equals(fieldName) ) {
				List<CodeListItem> items = codeListService.loadItems(attributeDefinition.getList(), attributeDefinition.getLevelPosition());
				int position = items.indexOf(item) + 1;
				return Integer.toString(position);
			} else {
				return item.getLabel(getConfig().getLanguageCode());
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
