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
	private static final String QUALIFIER_SUFFIX = "qualifier";
	
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
		//qualifier field
		CodeList list = defn.getList();
		if ( hasQualifiableItems(list) ) {
			result.add(CodeAttributeDefinition.QUALIFIER_FIELD);
		}
		//item position field
		if ( includeItemPositionColumn && ! list.isExternal() ) {
			result.add(ITEM_POSITION_FIELD_NAME);
		}
		return result.toArray(new String[]{});
	}

	@Override
	protected String getFieldHeading(String fieldName) {
		if ( CodeAttributeDefinition.CODE_FIELD.equals(fieldName) ) {
			return defn.getName();
		} else if ( CodeAttributeDefinition.QUALIFIER_FIELD.equals(fieldName) ) {
			return defn.getName() + FIELD_SEPARATOR + QUALIFIER_SUFFIX;
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
			if ( item == null ) {
				return "";
			} else {
				List<CodeListItem> items = codeListService.loadItems(defn.getList(), defn.getLevelPosition());
				int position = items.indexOf(item) + 1;
				return Integer.toString(position);
			}
		} else {
			return super.extractValue(attr, fieldName);
		}
	}

	private boolean hasQualifiableItems(CodeList list) {
		CodeListService codeListService = defn.getSurvey().getContext().getCodeListService();
		return codeListService.hasQualifiableItems(list);
	}
	
}
