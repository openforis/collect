/**
 * 
 */
package org.openforis.collect.csv;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CodeAttribute;

/**
 * @author M. Togna
 * 
 * @deprecated replaced with idm-transform api
 */
@Deprecated
public class CodeColumnProvider extends CompositeAttributeColumnProvider<CodeAttributeDefinition> {

	private static final String ITEM_POSITION_FIELD_NAME = "item_pos";
	private static final String ITEM_POSITION_SUFFIX = "class";
	
	private boolean includeItemPositionColumn;
	
	public CodeColumnProvider(CodeAttributeDefinition defn) {
		this(defn, false);
	}

	public CodeColumnProvider(CodeAttributeDefinition defn, boolean includeItemPositionColumn) {
		super(defn);
		this.includeItemPositionColumn = includeItemPositionColumn;
	}
	
	@Override
	protected String[] getFieldNames() {
		List<String> result = new ArrayList<String>();
		//code field
		result.add(CodeAttributeDefinition.CODE_FIELD);
		if ( includeItemPositionColumn && ! defn.getList().isExternal() ) {
			//item position field
			result.add(ITEM_POSITION_FIELD_NAME);
		}
		return result.toArray(new String[]{});
	}
	
	@Override
	protected String getFieldHeading(String fieldName) {
		if ( CodeAttributeDefinition.CODE_FIELD.equals(fieldName) ) {
			return defn.getName();
		} else if ( ITEM_POSITION_FIELD_NAME.equals(fieldName) ) {
			return "_" + defn.getName() + FIELD_SEPARATOR + ITEM_POSITION_SUFFIX;
		} else {
			return super.getFieldHeading(fieldName);
		}
	}
	
	@Override
	protected String extractValue(Attribute<?, ?> attr, String fieldName) {
		if ( ITEM_POSITION_FIELD_NAME.equals(fieldName) ) {
			SurveyContext context = defn.getSurvey().getContext();
			CodeListService codeListService = context.getCodeListService();
			CodeListItem item = codeListService.loadItem((CodeAttribute) attr);
			if ( item != null ) {
				List<CodeListItem> items = codeListService.loadItems(defn.getList(), defn.getLevelIndex());
				int position = items.indexOf(item) + 1;
				return Integer.toString(position);
			} else {
				return "";
			}
		} else {
			return super.extractValue(attr, fieldName);
		}
	}
	
}
