/**
 * 
 */
package org.openforis.collect.model;

import org.openforis.idm.model.TaxonOccurrence;

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
		setText2(scientificName);
		setText3(vernacularName);
		setText4(languageVariant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.TaxonOccurrence#getCode()
	 */
	@Override
	public String getCode() {
		return getText1();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.TaxonOccurrence#getScientificName()
	 */
	@Override
	public String getScientificName() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.TaxonOccurrence#getVernacularName()
	 */
	@Override
	public String getVernacularName() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.TaxonOccurrence#getLanguageCode()
	 */
	@Override
	public String getLanguageCode() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.TaxonOccurrence#getLanguageVariant()
	 */
	@Override
	public String getLanguageVariant() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFormatValid() {
		// TODO Auto-generated method stub
		return true;
	}

}
