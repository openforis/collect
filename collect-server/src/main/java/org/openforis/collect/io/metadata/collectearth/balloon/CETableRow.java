package org.openforis.collect.io.metadata.collectearth.balloon;


/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
class CETableRow extends CEFieldSet {

	private String keyValue;
	
	public CETableRow(String key, String label) {
		super(null, label);
		this.keyValue = key;
	}
	
	public String getKeyValue() {
		return keyValue;
	}
	
}