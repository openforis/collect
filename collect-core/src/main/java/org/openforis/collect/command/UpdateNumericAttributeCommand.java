package org.openforis.collect.command;

public class UpdateNumericAttributeCommand extends UpdateAttributeCommand {

	private static final long serialVersionUID = 1L;
	
	private Number value;
	private Integer unitId;

	public Number getValue() {
		return value;
	}
	
	public void setValue(Number value) {
		this.value = value;
	}
	
	public Integer getUnitId() {
		return unitId;
	}
	
	public void setUnitId(Integer unitId) {
		this.unitId = unitId;
	}
	
}
