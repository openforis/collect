package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.Unit;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class UnitFormObject extends ItemFormObject<Unit> {

	private String name;
	private String label;
	private String abbreviation;
	private String dimensionLabel;
	private Number conversionFactor;
	
	public enum Dimension {
		LENGTH, ANGLE, AREA, RATIO, CURRENCY, TIME;
		
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
	public void loadFrom(Unit source, String languageCode) {
		name = source.getName();
		label = source.getLabel(languageCode);
		abbreviation = source.getAbbreviation(languageCode);
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

	public Number getConversionFactor() {
		return conversionFactor;
	}

	public void setConversionFactor(Number conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	public String getDimensionLabel() {
		return dimensionLabel;
	}

	public void setDimensionLabel(String dimensionLabel) {
		this.dimensionLabel = dimensionLabel;
	}
	
}
