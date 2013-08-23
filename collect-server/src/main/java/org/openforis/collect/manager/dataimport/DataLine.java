package org.openforis.collect.manager.dataimport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.manager.referencedataimport.Line;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataLine extends Line {

	private Map<AttributeDefinition, String> recordKeysByDefn;
	private Map<AttributeDefinition, String> ancestorKeysByDefn;
	private Map<String, String> attributeValues;
	
	public DataLine() {
		recordKeysByDefn = new HashMap<AttributeDefinition, String>();
		ancestorKeysByDefn = new HashMap<AttributeDefinition, String>();
		attributeValues = new HashMap<String, String>();
	}
	
	public void addAncestorKey(AttributeDefinition keyDefn, String value) {
		if ( keyDefn.getParentEntityDefinition() == keyDefn.getRootEntity() ) {
			recordKeysByDefn.put(keyDefn, value);
		}
		ancestorKeysByDefn.put(keyDefn, value);
	}

	public void addAttributeValue(String name, String value) {
		attributeValues.put(name, value);
	}
	
	public String[] getRecordKeyValues(EntityDefinition rootEntityDefn) {
		List<AttributeDefinition> rootKeyAttrDefns = rootEntityDefn.getKeyAttributeDefinitions();
		String[] recordKeys = new String[rootKeyAttrDefns.size()];
		for (int i = 0; i < rootKeyAttrDefns.size(); i++) {
			AttributeDefinition keyDefn = rootKeyAttrDefns.get(i);
			String key = recordKeysByDefn.get(keyDefn);
			recordKeys[i] = key;
		}
		return recordKeys;
	}
	
	public Map<AttributeDefinition, String> getRecordKeys() {
		return recordKeysByDefn;
	}
	
	public Map<AttributeDefinition, String> getAncestorKeys() {
		return ancestorKeysByDefn;
	} 
	
	public Map<String, String> getAttributeValues() {
		return attributeValues;
	}
	
}
