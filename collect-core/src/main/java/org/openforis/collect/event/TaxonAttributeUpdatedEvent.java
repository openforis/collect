package org.openforis.collect.event;

import java.util.Date;

public class TaxonAttributeUpdatedEvent extends AttributeUpdatedEvent {
	
	private String code;
	private String scientificName;
	private String vernacularName;
	private String languageCode;
	private String languageVariety;
	
	public TaxonAttributeUpdatedEvent(Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, String code, String scientificName, String vernacularName,
			String languageCode, String languageVariety, Date timestamp, String userName) {
		super(recordId, definitionId, parentEntityId, nodeId, timestamp, userName);
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
