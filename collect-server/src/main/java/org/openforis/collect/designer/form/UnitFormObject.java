package org.openforis.collect.designer.form;

import java.util.Locale;

import org.openforis.collect.designer.viewmodel.UnitsVM;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.metamodel.Unit.Dimension;

/**
 * 
 * @author S. Ricci
 *
 */
public class UnitFormObject extends SurveyObjectFormObject<Unit> {

	private String name;
	private String label;
	private String abbreviation;
	private String dimensionLabel;
	private Double conversionFactor;
		
	@Override
	public void loadFrom(Unit source, String languageCode) {
		name = source.getName();
		label = source.getLabel(languageCode);
		abbreviation = source.getAbbreviation(languageCode);
		String dimensionValue = source.getDimension();
		if ( dimensionValue != null ) {
			Dimension dimension = Dimension.valueOf(dimensionValue.toUpperCase());
			dimensionLabel = UnitsVM.getDimensionLabel(dimension);
		} else {
			dimensionLabel = null;
		}
		conversionFactor = source.getConversionFactor();
	}
	
	@Override
	public void saveTo(Unit dest, String languageCode) {
		dest.setName(name);
		dest.setLabel(languageCode, label);
		dest.setAbbreviation(languageCode, abbreviation);
		Dimension dimension = getDimensionFromLabel(dimensionLabel);
		dest.setDimension(dimension == null ? null: dimension.name().toLowerCase(Locale.ENGLISH));
		dest.setConversionFactor(conversionFactor);
	}
	
	@Override
	protected void reset() {
		// TODO Auto-generated method stub
	}
	
	private Dimension getDimensionFromLabel(String label) {
		for (Dimension dimension : Dimension.values()) {
			String dimLabel = UnitsVM.getDimensionLabel(dimension);
			if ( dimLabel.equals(label) ) {
				return dimension;
			}
		}
		return null;
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

	public Double getConversionFactor() {
		return conversionFactor;
	}

	public void setConversionFactor(Double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	public String getDimensionLabel() {
		return dimensionLabel;
	}

	public void setDimensionLabel(String dimensionLabel) {
		this.dimensionLabel = dimensionLabel;
	}
	
}
