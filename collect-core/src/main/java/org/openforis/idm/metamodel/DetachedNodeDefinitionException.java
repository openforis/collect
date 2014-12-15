package org.openforis.idm.metamodel;

/**
 * @author G. Miceli
 */
public class DetachedNodeDefinitionException extends IllegalStateException {
	private static final long serialVersionUID = 1L;

	public DetachedNodeDefinitionException(Class<?> definitionClass, Class<?> containerClass) {
		super(definitionClass.getName()+" not attached to "+containerClass.getName());
	}
}
