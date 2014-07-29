/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.Calculable;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.validation.Check;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeDefinitionFormObject<T extends AttributeDefinition> extends NodeDefinitionFormObject<T> {

	public static final String ATTRIBUTE_DEFAULTS_FIELD = "attributeDefaults";
	public static final String CHECKS_FIELD = "checks";
	public static final String KEY_FIELD = "key";
		
	private List<AttributeDefault> attributeDefaults;
	private List<Check<?>> checks;
	private boolean includeInDataExport;
	private boolean showInUI;

	AttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
	}

	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.removeAllAttributeDefaults();
		if ( attributeDefaults != null ) {
			for (AttributeDefault attrDefault : attributeDefaults) {
				dest.addAttributeDefault(attrDefault);
			}
		}
		dest.removeAllChecks();
		if ( checks != null ) {
			for (Check<?> check : checks) {
				dest.addCheck(check);
			}
		}
		CollectSurvey survey = (CollectSurvey) dest.getSurvey();

		//include in data export
		CollectAnnotations annotations = survey.getAnnotations();
		annotations.setIncludeInDataExport(dest, includeInDataExport);
		
		//show in ui
		UIOptions uiOptions = survey.getUIOptions();
		uiOptions.setHidden(dest, ! showInUI);
	}
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		attributeDefaults = new ArrayList<AttributeDefault>(source.getAttributeDefaults());
		checks = new ArrayList<Check<?>>(source.getChecks());
		
		if ( source instanceof Calculable ) {
			CollectSurvey survey = (CollectSurvey) source.getSurvey();
			
			//show in UI
			UIOptions uiOptions = survey.getUIOptions();
			showInUI = ! uiOptions.isHidden(source);
			
			CollectAnnotations annotations = survey.getAnnotations();
			includeInDataExport = annotations.isIncludedInDataExport(source);
		}
	}
	
	@Override
	protected void reset() {
		super.reset();
		attributeDefaults = new ArrayList<AttributeDefault>();
		checks = null;
		showInUI = true;
		includeInDataExport = true;
	}

	public List<AttributeDefault> getAttributeDefaults() {
		return attributeDefaults;
	}
	
	public void setAttributeDefaults(List<AttributeDefault> attributeDefaults) {
		this.attributeDefaults = attributeDefaults;
	}

	public List<Check<?>> getChecks() {
		return checks;
	}

	public void setChecks(List<Check<?>> checks) {
		this.checks = checks;
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
}
