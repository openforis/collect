/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class Unit extends IdentifiableSurveyObject {

	private static final long serialVersionUID = 1L;

	private String name;
	private String dimension;
	private Double conversionFactor;
	private LanguageSpecificTextMap labels;
	private LanguageSpecificTextMap abbreviations;

	public enum Dimension {
		ANGLE, AREA, CURRENCY, LENGTH, MASS, RATIO, TIME;
	}
	
	Unit(Survey survey, int id) {
		super(survey, id);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDimension() {
		return this.dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}
	
	public Double getConversionFactor() {
		return this.conversionFactor;
	}

	public void setConversionFactor(Double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}
	
	public List<LanguageSpecificText> getLabels() {
		if ( labels == null ) {
			return Collections.emptyList();
		} else {
			return labels.values();
		}
	}
	
	public String getLabel(String language) {
		return labels == null ? null: labels.getText(language);
	}
	
	public void addLabel(LanguageSpecificText label) {
		if ( labels == null ) {
			labels = new LanguageSpecificTextMap();
		}
		labels.add(label);
	}

	public void setLabel(String language, String text) {
		if ( labels == null ) {
			labels = new LanguageSpecificTextMap();
		}
		labels.setText(language, text);
	}
	
	public void removeLabel(String language) {
		labels.remove(language);
	}

	public List<LanguageSpecificText> getAbbreviations() {
		if ( abbreviations == null ) {
			return Collections.emptyList();
		} else {
			return abbreviations.values();
		}
	}
	
	public String getAbbreviation(String language) {
		return abbreviations == null ? null: abbreviations.getText(language);
	}
	
	public void addAbbreviation(LanguageSpecificText label) {
		if ( abbreviations == null ) {
			abbreviations = new LanguageSpecificTextMap();
		}
		abbreviations.add(label);
	}

	public void setAbbreviation(String language, String text) {
		if ( abbreviations == null ) {
			abbreviations = new LanguageSpecificTextMap();
		}
		abbreviations.setText(language, text);
	}
	
	public void removeAbbreviation(String language) {
		abbreviations.remove(language);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("name", name)
			.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((abbreviations == null) ? 0 : abbreviations.hashCode());
		result = prime * result + ((conversionFactor == null) ? 0 : conversionFactor.hashCode());
		result = prime * result + ((dimension == null) ? 0 : dimension.hashCode());
		result = prime * result + ((labels == null) ? 0 : labels.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Unit other = (Unit) obj;
		if (abbreviations == null) {
			if (other.abbreviations != null)
				return false;
		} else if (!abbreviations.equals(other.abbreviations))
			return false;
		if (conversionFactor == null) {
			if (other.conversionFactor != null)
				return false;
		} else if (!conversionFactor.equals(other.conversionFactor))
			return false;
		if (dimension == null) {
			if (other.dimension != null)
				return false;
		} else if (!dimension.equals(other.dimension))
			return false;
		if (labels == null) {
			if (other.labels != null)
				return false;
		} else if (!labels.equals(other.labels))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
}
