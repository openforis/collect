package org.openforis.collect.io.metadata.parsing;

import java.util.Map;

import org.openforis.commons.collection.CollectionUtils;

public class ReferenceDataLine extends Line {

	private Map<String, String> infoAttributeByName;
	
	public Map<String, String> getInfoAttributeByName() {
		return CollectionUtils.unmodifiableMap(infoAttributeByName);
	}
	
	public String getInfoAttribute(String name) {
		if ( infoAttributeByName == null ) {
			return null;
		} else {
			return infoAttributeByName.get(name);
		}
	}
	
	public void setInfoAttributeByName(Map<String, String> infoAttributeByName) {
		this.infoAttributeByName = infoAttributeByName;
	}

}
