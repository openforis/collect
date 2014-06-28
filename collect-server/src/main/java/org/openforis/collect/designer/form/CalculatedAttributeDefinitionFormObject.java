/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition.Formula;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition.Type;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 *
 */
public class CalculatedAttributeDefinitionFormObject<T extends CalculatedAttributeDefinition> extends AttributeDefinitionFormObject<T> {

	private String type;
	private boolean includeInDataExport;
	private boolean showInUI;
	private List<Formula> formulas;
	
	CalculatedAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
		reset();
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setType(Type.valueOf(type));
		dest.setFormulas(formulas);
		
		CollectSurvey survey = (CollectSurvey) dest.getSurvey();

		//include in data export
		CollectAnnotations annotations = survey.getAnnotations();
		annotations.setIncludeInDataExport(dest, includeInDataExport);
		
		//show in ui
		UIOptions uiOptions = survey.getUIOptions();
		uiOptions.setShowInUI(dest, showInUI);
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		Type typeEnum = source.getType();
		type = typeEnum.name();
		formulas = new ArrayList<CalculatedAttributeDefinition.Formula>(source.getFormulas());
		
		CollectSurvey survey = (CollectSurvey) source.getSurvey();
		
		//show in UI
		UIOptions uiOptions = survey.getUIOptions();
		showInUI = uiOptions.isShownInUI(source);
		
		CollectAnnotations annotations = survey.getAnnotations();
		includeInDataExport = annotations.isIncludedInDataExport(source);
	}

	@Override
	protected void reset() {
		super.reset();
		type = Type.DEFAULT.name();
		includeInDataExport = true;
		showInUI = false;
		formulas = new ArrayList<CalculatedAttributeDefinition.Formula>();
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public boolean isIncludeInDataExport() {
		return includeInDataExport;
	}
	
	public void setIncludeInDataExport(boolean includeInDataExport) {
		this.includeInDataExport = includeInDataExport;
	}
	
	public boolean isShowInUI() {
		return showInUI;
	}
	
	public void setShowInUI(boolean showInUI) {
		this.showInUI = showInUI;
	}
	
	public List<Formula> getFormulas() {
		return formulas;
	}
	
	public void setFormulas(List<Formula> formulas) {
		this.formulas = formulas;
	}
	
}
