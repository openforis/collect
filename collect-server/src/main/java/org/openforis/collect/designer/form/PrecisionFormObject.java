package org.openforis.collect.designer.form;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.Precision;

/**
 * 
 * @author S. Ricci
 *
 */
public class PrecisionFormObject extends FormObject<Precision> {

	private String unitName;
	private Integer decimalDigits;
	private boolean defaultPrecision;
	// optional (empty when created by ZK
	private CollectSurvey survey;

	public PrecisionFormObject() {
	}
	
	public PrecisionFormObject(CollectSurvey survey) {
		this.survey = survey;
	}

	@Override
	public void loadFrom(Precision source, String languageCode) {
		unitName = source.getUnitName();
		decimalDigits = source.getDecimalDigits();
		defaultPrecision = source.isDefaultPrecision();
	}

	@Override
	public void saveTo(Precision dest, String languageCode) {
		dest.setUnit(unitName == null ? null : survey.getUnit(unitName));
		dest.setDecimalDigits(decimalDigits);
		dest.setDefaultPrecision(defaultPrecision);
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public Integer getDecimalDigits() {
		return decimalDigits;
	}

	public void setDecimalDigits(Integer decimalDigits) {
		this.decimalDigits = decimalDigits;
	}

	public boolean isDefaultPrecision() {
		return defaultPrecision;
	}

	public void setDefaultPrecision(boolean defaultPrecision) {
		this.defaultPrecision = defaultPrecision;
	}

}
