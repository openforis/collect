/**
 * 
 */
package org.openforis.collect.idm.model.impl;

/**
 * @author M. Togna
 * 
 */
public class Taxon extends AbstractValue implements org.openforis.idm.model.Taxon {

	private String code;
	private String scientificName;
	private String vernacularName;
	private String languageCode;
	private String languageVariant;

	public Taxon(String stringValue) {
		super(stringValue);
	}

	@Override
	public boolean isBlank() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public String getScientificName() {
		return this.scientificName;
	}

	@Override
	public String getVernacularName() {
		return this.vernacularName;
	}

	@Override
	public String getLanguageCode() {
		return this.languageCode;
	}

	@Override
	public String getLanguageVariant() {
		return this.languageVariant;
	}

}
