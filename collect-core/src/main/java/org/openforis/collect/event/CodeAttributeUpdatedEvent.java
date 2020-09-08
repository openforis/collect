package org.openforis.collect.event;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class CodeAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private String code;
	private String qualifier;

	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}

	public String getQualifier() {
		return qualifier;
	}
	
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
}
