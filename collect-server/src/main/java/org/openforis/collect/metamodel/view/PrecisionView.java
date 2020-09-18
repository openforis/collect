package org.openforis.collect.metamodel.view;

import org.openforis.idm.metamodel.Precision;

public class PrecisionView {
	
	private int unitId;
	private Integer decimalDigits;
	private boolean defaultPrecision;

	public PrecisionView(Precision precision) {
		this.unitId = precision.getUnit().getId();
		this.decimalDigits = precision.getDecimalDigits();
		this.defaultPrecision = precision.isDefaultPrecision();
	}
	
	public int getUnitId() {
		return unitId;
	}
	
	public Integer getDecimalDigits() {
		return decimalDigits;
	}
	
	public boolean isDefaultPrecision() {
		return defaultPrecision;
	}

}
