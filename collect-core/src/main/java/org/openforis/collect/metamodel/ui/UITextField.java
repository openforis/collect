package org.openforis.collect.metamodel.ui;

public class UITextField extends UIField {
	
	public enum TextTransform {
		NONE, UPPERCASE, LOWERCASE, CAMELCASE;
	}

	private static final long serialVersionUID = 1L;
	
	private TextTransform textTranform;

	<P extends UIFormContentContainer> UITextField(P parent, int id) {
		super(parent, id);
	}

	public TextTransform getTextTranform() {
		return textTranform;
	}
	
	public void setTextTranform(TextTransform textTranform) {
		this.textTranform = textTranform;
	}
	
}
