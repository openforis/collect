package org.openforis.idm.metamodel;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 *
 */
public class NodeLabel extends TypedLanguageSpecificText<NodeLabel.Type> {

	public NodeLabel(Type type, String language, String text) {
		super(type, language, text);
	}

	private static final long serialVersionUID = 1L;

	public enum Type {
		HEADING, INSTANCE, NUMBER;
	}

}