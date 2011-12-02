/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.openforis.idm.model.Taxon;

/**
 * @author M. Togna
 * 
 */
public class TaxonImpl extends AbstractValue implements Taxon {

	public TaxonImpl(String code) {
		super(code);
	}

	public TaxonImpl(String code, String scientificName, String vernacularName, String languageVariant) {
		this(code);
		setValue2(scientificName);
		setValue3(vernacularName);
		setValue4(languageVariant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.Taxon#getCode()
	 */
	@Override
	public String getCode() {
		return getValue1();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.Taxon#getScientificName()
	 */
	@Override
	public String getScientificName() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.Taxon#getVernacularName()
	 */
	@Override
	public String getVernacularName() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.Taxon#getLanguageCode()
	 */
	@Override
	public String getLanguageCode() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.Taxon#getLanguageVariant()
	 */
	@Override
	public String getLanguageVariant() {
		// TODO Auto-generated method stub
		return null;
	}

}
