/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.openforis.idm.model.Node;

/**
 * @author M. Togna
 * @author G. Miceli
 */
public interface ValidationRule<N extends Node<?>> {

	ValidationResultFlag evaluate(N node);
	
}
