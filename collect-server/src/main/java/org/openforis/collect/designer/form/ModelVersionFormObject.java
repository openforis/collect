package org.openforis.collect.designer.form;

import java.util.Date;

import org.openforis.idm.metamodel.ModelVersion;

/**
 * 
 * @author S. Ricci
 *
 */
public class ModelVersionFormObject extends SurveyObjectFormObject<ModelVersion> {

	private String name;
	private String label;
	private String description;
	private Date date;
	
	@Override
	public void loadFrom(ModelVersion source, String languageCode) {
		name = source.getName();
		label = source.getLabel(languageCode);
		description = source.getDescription(languageCode);
		date = source.getDate();
	}
	
	@Override
	public void saveTo(ModelVersion dest, String languageCode) {
		dest.setName(name);
		dest.setLabel(languageCode, label);
		dest.setDescription(languageCode, description);
		dest.setDate(date);
	}
	
	@Override
	protected void reset() {
		// TODO 
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
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
}
