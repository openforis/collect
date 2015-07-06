package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

public class TaxonAttributeUpdatedEvent extends AttributeUpdatedEvent {
	
	private String code;
	private String scientificName;
	private String vernacularName;
	private String languageCode;
	private String languageVariety;
	
	public TaxonAttributeUpdatedEvent(String surveyName, Integer recordId, String definitionId, List<String> ancestorIds, 
			String nodeId, String code, String scientificName, String vernacularName,
			String languageCode, String languageVariety, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, ancestorIds, nodeId, timestamp, userName);
		this.code = code;
		this.scientificName = scientificName;
		this.vernacularName = vernacularName;
		this.languageCode = languageCode;
		this.languageVariety = languageVariety;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getScientificName() {
		return scientificName;
	}
	
	public String getVernacularName() {
		return vernacularName;
	}
	
	public String getLanguageCode() {
		return languageCode;
	}
	
	public String getLanguageVariety() {
		return languageVariety;
	}
	
}
