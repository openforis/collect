package org.openforis.collect.io.metadata.collectearth.balloon;


/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
class CETableRow extends CEFieldSet {

	private String keyValue;
	
	private String tooltip;
	
	public CETableRow(String key, String label, String tooltip) {
		super(null, label);
		this.keyValue = key;
		this.tooltip = tooltip;
	}
	
	public String getKeyValue() {
		return keyValue;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
	
}