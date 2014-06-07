/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeDefinitionFormObject extends AttributeDefinitionFormObject<CodeAttributeDefinition> {
	
	private boolean key;
	private CodeList list;
	private CodeAttributeDefinition parentCodeAttributeDefinition;
	private boolean strict;
	private boolean allowValuesSorting;
	private boolean showAllowedValuesPreview;
	
	CodeAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
		strict = true;
		allowValuesSorting = false;
		showAllowedValuesPreview = false;
	}

	@Override
	public void saveTo(CodeAttributeDefinition dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setList(list);
		dest.setKey(key);
		dest.setAllowUnlisted(! strict);
		dest.setParentCodeAttributeDefinition(parentCodeAttributeDefinition);
		dest.setAllowValuesSorting(dest.isMultiple() && allowValuesSorting);
		
		CollectSurvey survey = (CollectSurvey) dest.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		uiOptions.setShowAllowedValuesPreviewValue(dest, showAllowedValuesPreview);
	}
	
	@Override
	public void loadFrom(CodeAttributeDefinition source, String languageCode) {
		super.loadFrom(source, languageCode);
		key = source.isKey();
		list = source.getList();
		parentCodeAttributeDefinition = source.getParentCodeAttributeDefinition();
		strict = ! source.isAllowUnlisted();
		allowValuesSorting = source.isMultiple() && source.isAllowValuesSorting();
		
		CollectSurvey survey = (CollectSurvey) source.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		showAllowedValuesPreview = uiOptions.getShowAllowedValuesPreviewValue(source);
	}

	public boolean isKey() {
		return key;
	}

	public void setKey(boolean key) {
		this.key = key;
	}

	public CodeList getList() {
		return list;
	}

	public void setList(CodeList list) {
		this.list = list;
	}

	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}
	
	public boolean isAllowValuesSorting() {
		return allowValuesSorting;
	}
	
	public void setAllowValuesSorting(boolean allowValuesSorting) {
		this.allowValuesSorting = allowValuesSorting;
	}
	
	public CodeAttributeDefinition getParentCodeAttributeDefinition() {
		return parentCodeAttributeDefinition;
	}
	
	public void setParentCodeAttributeDefinition(CodeAttributeDefinition parentCodeAttributeDefinition) {
		this.parentCodeAttributeDefinition = parentCodeAttributeDefinition;
	}
	
	public boolean isShowAllowedValuesPreview() {
		return showAllowedValuesPreview;
	}

	public void setShowAllowedValuesPreview(boolean showAllowedValuesPreview) {
		this.showAllowedValuesPreview = showAllowedValuesPreview;
	}
}
