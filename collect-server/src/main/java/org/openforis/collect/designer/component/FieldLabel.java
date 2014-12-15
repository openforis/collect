/**
 * 
 */
package org.openforis.collect.designer.component;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zul.Label;

/**
 * @author S. Ricci
 *
 */
public class FieldLabel extends Label {

	private static final long serialVersionUID = 1L;

	private String value;
	private String languageCode;

	public FieldLabel() {
		super();
	}

	public String getLanguageCode() {
		return languageCode;
	}
	
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
		buildValue();
	}
	
	@Override
	public void setValue(String value) {
		this.value = value;
		buildValue();
	}
	
	protected void buildValue() {
		StringBuilder sb = new StringBuilder();
		if ( StringUtils.isNotBlank(this.value) ) {
			sb.append(this.value);
			if ( StringUtils.isNotBlank(this.languageCode) ) {
				sb.append(" ");
				sb.append("(");
				sb.append(this.languageCode);
				sb.append(")");
			}
			sb.append(":");
		}
		super.setValue(sb.toString());
	}
	
}
