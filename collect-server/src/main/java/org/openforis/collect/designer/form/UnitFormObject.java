package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.Unit;

/**
 * 
 * @author S. Ricci
 *
 */
public class UnitFormObject extends ItemFormObject<Unit> {

	private String name;
	private String label;
	private String abbreviation;
	private String dimension;
	private Number conversionFactor;
	
	@Override
	public void loadFrom(Unit source, String languageCode) {
		name = source.getName();
		label = source.getLabel(languageCode);
		abbreviation = source.getAbbreviation(languageCode);
		dimension = source.getDimension();
		conversionFactor = source.getConversionFactor();
	}
	
	@Override
	public void saveTo(Unit dest, String languageCode) {
		dest.setName(name);
		dest.setLabel(languageCode, label);
		dest.setAbbreviation(languageCode, abbreviation);
		dest.setDimension(dimension);
		dest.setConversionFactor(conversionFactor != null ? conversionFactor.floatValue(): null);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getDimension() {
		return dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public Number getConversionFactor() {
		return conversionFactor;
	}

	public void setConversionFactor(Number conversionFactor) {
		this.conversionFactor = conversionFactor;
	}
	
}
