package org.openforis.collect.io.metadata.collectearth.balloon;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
class CEField extends CEComponent {
	
	public enum CEFieldType {
		BOOLEAN, COORDINATE, CODE_SELECT, CODE_BUTTON_GROUP, DATE, INTEGER, REAL, SHORT_TEXT, LONG_TEXT, TIME, CODE_RANGE
	}
	
	private CEFieldType type;
	private boolean key;
	private boolean readOnly = false;
	private String tooltip;
	
	public CEField(String htmlParameterName, String name, String label, String tooltip, boolean multiple, CEField.CEFieldType type, boolean key) {
		super(htmlParameterName, name, label, multiple);
		this.type = type;
		this.key = key;
		this.tooltip = tooltip;
	}

	public CEFieldType getType() {
		return type;
	}

	public boolean isKey() {
		return key;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void setTooltip(String tooltip){
		this.tooltip = tooltip;
	}
	
	public String getToolTip() {
		return tooltip;
	}
	
}