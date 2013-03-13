package org.openforis.collect.designer.form;

import java.util.Date;

import org.openforis.collect.util.DateUtil;
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
	public void loadFrom(ModelVersion source, String languageCode, String defaultLanguage) {
		name = source.getName();
		label = getLabel(source, languageCode, defaultLanguage);
		description = getDescription(source, languageCode, defaultLanguage);
		date = DateUtil.parseXMLDateTime(source.getDate());
	}
	
	@Override
	public void saveTo(ModelVersion dest, String languageCode) {
		dest.setName(name);
		dest.setLabel(languageCode, label);
		dest.setDescription(languageCode, description);
		dest.setDate(DateUtil.formatDateToXML(date));
	}
	
	@Override
	protected void reset() {
		// TODO 
	}
	
	protected String getLabel(ModelVersion source, String languageCode, String defaultLanguage) {
		String result = source.getLabel(languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getLabel(null);
		}
		return result;
	}

	protected String getDescription(ModelVersion source, String languageCode, String defaultLanguage) {
		String result = source.getDescription(languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getDescription(null);
		}
		return result;
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
