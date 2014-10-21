/**
 * 
 */
package org.openforis.idm.metamodel;


/**
 * @author G. Miceli
 * @author M. Togna
 */
public class CodeListLabel extends TypedLanguageSpecificText<CodeListLabel.Type> {

	private static final long serialVersionUID = 1L;

	public enum Type { ITEM, LIST }
	
	public CodeListLabel(Type type, String language, String text) {
		super(type, language, text);
	}
	
}
