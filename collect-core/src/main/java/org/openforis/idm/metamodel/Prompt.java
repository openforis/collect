/**
 * 
 */
package org.openforis.idm.metamodel;


/**
 * @author G. Miceli
 * @author M. Togna
 */
public class Prompt extends TypedLanguageSpecificText<Prompt.Type> {

	private static final long serialVersionUID = 1L;

	public enum Type {
		INTERVIEW, PAPER, HANDHELD, PC;
	}
	
	public Prompt(Type type, String language, String text) {
		super(type, language, text);
	}

}
