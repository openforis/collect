package org.openforis.collect.designer.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.Unit;

/**
 * 
 * @author S. Ricci
 *
 */
public class PrecisionFormObject extends FormObject<Precision> {

	private Unit unit;
	private Integer decimalDigits;
	private boolean defaultPrecision;

	public static List<PrecisionFormObject> fromList(List<Precision> precisionDefinitions, String languageCode, String defaultLanguage) {
		ArrayList<PrecisionFormObject> result = new ArrayList<PrecisionFormObject>();
		for (Precision precision : precisionDefinitions) {
			PrecisionFormObject formObject = new PrecisionFormObject();
			formObject.loadFrom(precision, languageCode, defaultLanguage);
			result.add(formObject);
		}
		return result;
	}

	@Override
	public void loadFrom(Precision source, String languageCode, String defaultLanguage) {
		unit = source.getUnit();
		decimalDigits = source.getDecimalDigits();
		defaultPrecision = source.isDefaultPrecision();
	}

	@Override
	public void saveTo(Precision dest, String languageCode) {
		dest.setUnit(unit);
		dest.setDecimalDigits(decimalDigits);
		dest.setDefaultPrecision(defaultPrecision);
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub
	}
	
	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
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
