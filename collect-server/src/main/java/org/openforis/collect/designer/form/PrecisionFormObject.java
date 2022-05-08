package org.openforis.collect.designer.form;

import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.Precision;

/**
 * 
 * @author S. Ricci
 *
 */
public class PrecisionFormObject extends FormObject<Precision> {

	private UnitFormObject unit;
	private Integer decimalDigits;
	private boolean defaultPrecision;
	// optional (empty when created by ZK
	private CollectSurvey survey;
	private List<UnitFormObject> units;

	public PrecisionFormObject() {
	}
	
	public PrecisionFormObject(CollectSurvey survey, List<UnitFormObject> units) {
		this.survey = survey;
		this.units = units;
	}

	@Override
	public void loadFrom(Precision source, String languageCode) {
		unit = source.getUnit() == null ? null : CollectionUtils.findItem(units, source.getUnit().getName(), "name");
		decimalDigits = source.getDecimalDigits();
		defaultPrecision = source.isDefaultPrecision();
	}

	@Override
	public void saveTo(Precision dest, String languageCode) {
		dest.setUnit(unit == null ? null : survey.getUnit(unit.getName()));
		dest.setDecimalDigits(decimalDigits);
		dest.setDefaultPrecision(defaultPrecision);
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub
	}

	public UnitFormObject getUnit() {
		return unit;
	}

	public void setUnit(UnitFormObject unit) {
		this.unit = unit;
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
