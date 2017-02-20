/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
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
	public static final String MEASUREMENT_FIELD = "measurement";
	public static final String CALCULATED_FIELD = "calculated";
	public static final String REFERENCED_ATTRIBUTE_PATH_FIELD = "referencedAttributePath";

	private List<AttributeDefault> attributeDefaults;
	private String phaseToApplyDefaultValue;
	private String referencedAttributePath;
	private boolean editable;
	private List<Check<?>> checks;
	private String[] visibleFields;

	AttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
	}

	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);

		dest.setKey(key);

		// save attribute defaults
		dest.removeAllAttributeDefaults();
		if (attributeDefaults != null) {
			for (AttributeDefault attrDefault : attributeDefaults) {
				dest.addAttributeDefault(attrDefault);
			}
		}
		dest.setReferencedAttribute(referencedAttributePath == null ? null
				: (AttributeDefinition) dest.getSchema().getDefinitionByPath(referencedAttributePath));

		CollectSurvey survey = (CollectSurvey) dest.getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();
		annotations.setPhaseToApplyDefaultValue(dest, Step.valueOf(phaseToApplyDefaultValue));
		annotations.setEditable(dest, editable);
		annotations.setMeasurementAttribute(dest, measurement);

		// save checks
		dest.removeAllChecks();
		if (checks != null) {
			for (Check<?> check : checks) {
				dest.addCheck(check);
			}
		}

		UIOptions uiOptions = getUIOptions(dest);
		uiOptions.setVisibleFields(dest, visibleFields);
	}

	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);

		key = source.isKey();

		attributeDefaults = new ArrayList<AttributeDefault>(source.getAttributeDefaults());

		CollectSurvey survey = (CollectSurvey) source.getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();

		phaseToApplyDefaultValue = annotations.getPhaseToApplyDefaultValue(source).name();
		editable = annotations.isEditable(source);
		measurement = annotations.isMeasurementAttribute(source);
		
		checks = new ArrayList<Check<?>>(source.getChecks());

		UIOptions uiOptions = getUIOptions(source);
		visibleFields = uiOptions.getVisibleFields(source);
		if (source.getReferencedAttribute() != null) {
			referencedAttributePath = source.getReferencedAttribute().getPath();
		}
	}

	@Override
	protected void reset() {
		super.reset();
		attributeDefaults = new ArrayList<AttributeDefault>();
		phaseToApplyDefaultValue = ((Step) Annotation.PHASE_TO_APPLY_DEFAULT_VALUE.getDefaultValue()).name();
		checks = null;
	}

	public List<AttributeDefault> getAttributeDefaults() {
		return attributeDefaults;
	}

	public void setAttributeDefaults(List<AttributeDefault> attributeDefaults) {
		this.attributeDefaults = attributeDefaults;
	}

	public String getPhaseToApplyDefaultValue() {
		return phaseToApplyDefaultValue;
	}

	public void setPhaseToApplyDefaultValue(String phaseToApplyDefaultValue) {
		this.phaseToApplyDefaultValue = phaseToApplyDefaultValue;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public List<Check<?>> getChecks() {
		return checks;
	}

	public void setChecks(List<Check<?>> checks) {
		this.checks = checks;
	}

	public String[] getVisibleFields() {
		return visibleFields;
	}

	public void setVisibleFields(String[] visibleFields) {
		this.visibleFields = visibleFields;
	}

	public String getReferencedAttributePath() {
		return referencedAttributePath;
	}

	public void setReferencedAttributePath(String referencedAttributePath) {
		this.referencedAttributePath = referencedAttributePath;
	}
}
