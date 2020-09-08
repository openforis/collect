package org.openforis.collect.event;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class TextAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private String text;

	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

}
