package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.SpatialReferenceSystem;

/**
 * 
 * @author S. Ricci
 *
 */
public class SpatialReferenceSystemFormObject extends FormObject<SpatialReferenceSystem> {

	private String id;
	private String label;
	private String description;
	private String wellKnownText;
	
	@Override
	public void loadFrom(SpatialReferenceSystem source, String languageCode, String defaultLanguage) {
		id = source.getId();
		label = getLabel(source, languageCode, defaultLanguage);
		description = getDescription(source, languageCode, defaultLanguage);
		wellKnownText = source.getWellKnownText();
	}
	
	@Override
	public void saveTo(SpatialReferenceSystem dest, String languageCode) {
		dest.setId(id);
		dest.setLabel(languageCode, label);
		dest.setDescription(languageCode, description);
		dest.setWellKnownText(wellKnownText);
	}
	
	@Override
	protected void reset() {
		// TODO Auto-generated method stub
	}

	protected String getLabel(SpatialReferenceSystem source, String languageCode, String defaultLanguage) {
		String result = source.getLabel(languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getLabel(null);
		}
		return result;
	}

	protected String getDescription(SpatialReferenceSystem source, String languageCode, String defaultLanguage) {
		String result = source.getDescription(languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getDescription(null);
		}
		return result;
	}
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getWellKnownText() {
		return wellKnownText;
	}

	public void setWellKnownText(String wellKnownText) {
		this.wellKnownText = wellKnownText;
	}

}
