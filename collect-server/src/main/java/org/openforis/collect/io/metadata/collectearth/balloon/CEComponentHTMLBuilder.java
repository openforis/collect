package org.openforis.collect.io.metadata.collectearth.balloon;


/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
public class CEComponentHTMLBuilder {

	private StringBuilder sb = new StringBuilder();
	
	public void append(CEComponent comp) {
		if (comp instanceof CEField) {
			String parameterName = comp.getHtmlParameterName();
			sb.append("<input type=\"hidden\" id=\"" + parameterName + "\" name=\"" + parameterName + "\"  />");
			sb.append('\n');
		}
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
}
