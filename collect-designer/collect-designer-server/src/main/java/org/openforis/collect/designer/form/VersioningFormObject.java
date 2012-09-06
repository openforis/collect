package org.openforis.collect.designer.form;

import java.util.Date;

import org.openforis.idm.metamodel.ModelVersion;

/**
 * 
 * @author S. Ricci
 *
 */
public class VersioningFormObject extends ItemFormObject<ModelVersion> {

	private String name;
	private String label;
	private String description;
	private Date date;
	
	@Override
	public void setValues(ModelVersion source, String languageCode) {
		
	}
	@Override
	public void copyValues(ModelVersion dest, String languageCode) {
		// TODO Auto-generated method stub
		
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
