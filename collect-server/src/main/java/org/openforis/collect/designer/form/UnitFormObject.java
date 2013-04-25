package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.Unit;
import org.zkoss.util.resource.Labels;

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
	
	public enum Dimension {
		ANGLE, AREA, CURRENCY, LENGTH, MASS, RATIO, TIME;
		
		public String getLabel() {
			String labelKey = "survey.unit.dimension." + this.name().toLowerCase();
			String label = Labels.getLabel(labelKey);
			return label;
		}
		
		public static Dimension fromLabel(String label) {
			Dimension[] dimensions = values();
			for (Dimension dimension : dimensions) {
				String dimLabel = dimension.getLabel();
				if ( dimLabel.equals(label) ) {
					return dimension;
				}
			}
			return null;
		}
	}
	
	@Override
	public void loadFrom(Unit source, String languageCode, String defaultLanguage) {
		name = source.getName();
		label = getLabel(source, languageCode, defaultLanguage);
		abbreviation = getAbbreviation(source, languageCode, defaultLanguage);
		String dimensionValue = source.getDimension();
		if ( dimensionValue != null ) {
			Dimension dimension = Dimension.valueOf(dimensionValue.toUpperCase());
			dimensionLabel = dimension.getLabel();
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
		Dimension dimension = Dimension.fromLabel(dimensionLabel);
		dest.setDimension(dimension != null ? dimension.name().toLowerCase(): null);
		dest.setConversionFactor(conversionFactor);
	}
	
	@Override
	protected void reset() {
		// TODO Auto-generated method stub
	}
	
	protected String getLabel(Unit source, String languageCode, String defaultLanguage) {
		String result = source.getLabel(languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getLabel(null);
		}
		return result;
	}

	protected String getAbbreviation(Unit source, String languageCode, String defaultLanguage) {
		String result = source.getAbbreviation(languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getAbbreviation(null);
		}
		return result;
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
