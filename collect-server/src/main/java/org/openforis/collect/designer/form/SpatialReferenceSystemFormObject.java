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
	public void loadFrom(SpatialReferenceSystem source, String languageCode) {
		id = source.getId();
		label = source.getLabel(languageCode,null);
		description = source.getDescription(languageCode,null);
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
