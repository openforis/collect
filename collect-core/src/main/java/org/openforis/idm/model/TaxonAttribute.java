package org.openforis.idm.model;

import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.Languages.Standard;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class TaxonAttribute extends Attribute<TaxonAttributeDefinition, TaxonOccurrence> {

	private static final long serialVersionUID = 1L;
	
	public TaxonAttribute(TaxonAttributeDefinition definition) {
		super(definition);
	}
	
	@SuppressWarnings("unchecked")
	public Field<String> getCodeField() {
		return (Field<String>) getField(0);
	}

	@SuppressWarnings("unchecked")
	public Field<String> getScientificNameField() {
		return (Field<String>) getField(1);
	}

	@SuppressWarnings("unchecked")
	public Field<String> getVernacularNameField() {
		return (Field<String>) getField(2);
	}

	@SuppressWarnings("unchecked")
	public Field<String> getLanguageCodeField() {
		return (Field<String>) getField(3);
	}

	@SuppressWarnings("unchecked")
	public Field<String> getLanguageVarietyField() {
		return (Field<String>) getField(4);
	}
	
	@SuppressWarnings("unchecked")
	public Field<String> getFamilyCodeField() {
		return (Field<String>) getField(5);
	}
	
	@SuppressWarnings("unchecked")
	public Field<String> getFamilyScientificNameField() {
		return (Field<String>) getField(6);
	}
	
	public String getCode() {
		return getCodeField().getValue();
	}
	
	public void setCode(String value) {
		getCodeField().setValue(value);
	}
	
	public String getScientificName() {
		return getScientificNameField().getValue();
	}
	
	public void setScientificName(String name) {
		getScientificNameField().setValue(name);
	}

	public String getVernacularName() {
		return getVernacularNameField().getValue();
	}
	
	public void setVernacularName(String name) {
		getVernacularNameField().setValue(name);
	}

	public String getLanguageCode() {
		return getLanguageCodeField().getValue();
	}
	
	public void setLanguageCode(String code) {
		checkValidLanguageCode(code);
		getLanguageCodeField().setValue(code);
	}
	
	public String getFamilyCode() {
		return getFamilyCodeField().getValue();
	}
	
	public void setFamilyCode(String code) {
		getFamilyCodeField().setValue(code);
	}
	
	public String getFamilyScientificName() {
		return getFamilyScientificNameField().getValue();
	}
	
	public void setFamilyScientificName(String familyName) {
		getFamilyScientificNameField().setValue(familyName);
	}

	private void checkValidLanguageCode(String code) {
		if ( code != null && ! Languages.exists(Standard.ISO_639_3, code) ) {
			throw new LanguageCodeNotSupportedException("Language code not supported: " + code);
		}
	}

	public String getLanguageVariety() {
		return getLanguageVarietyField().getValue();
	}
	
	public void setLanguageVariety(String var) {
		getLanguageVarietyField().setValue(var);
	}
	
	@Override
	public TaxonOccurrence getValue() {
		String code = getCodeField().getValue();
		String scientificName = getScientificName();
		String vernacularName = getVernacularName();
		String languageCode = getLanguageCode();
		String languageVariety = getLanguageVariety();
		TaxonOccurrence taxonOccurrence = new TaxonOccurrence(code, scientificName, vernacularName, languageCode, languageVariety);
		taxonOccurrence.setFamilyCode(getFamilyCode());
		taxonOccurrence.setFamilyScientificName(getFamilyScientificName());
		return taxonOccurrence;
	}

	@Override
	protected void setValueInFields(TaxonOccurrence value) {
		checkValidLanguageCode(value.getLanguageCode());

		setCode(value.getCode());
		setScientificName(value.getScientificName());
		setVernacularName(value.getVernacularName());
		setLanguageCode(value.getLanguageCode());
		setLanguageVariety(value.getLanguageVariety());
		TaxonOccurrence familyAncestor = value.getAncestorTaxon(TaxonRank.FAMILY);
		if (familyAncestor == null) {
			setFamilyCode(value.getFamilyCode());
			setFamilyScientificName(value.getFamilyScientificName());
		} else {
			setFamilyCode(familyAncestor.getCode());
			setFamilyScientificName(familyAncestor.getScientificName());
		}
	}
	
	public static class LanguageCodeNotSupportedException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public LanguageCodeNotSupportedException() {
			super();
		}

		public LanguageCodeNotSupportedException(String message, Throwable cause) {
			super(message, cause);
		}

		public LanguageCodeNotSupportedException(String message) {
			super(message);
		}

		public LanguageCodeNotSupportedException(Throwable cause) {
			super(cause);
		}
		
	}
}
