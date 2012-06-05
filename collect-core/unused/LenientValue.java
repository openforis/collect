package org.openforis.collect.model;

import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public interface LenientValue extends Value {
	boolean isBlank();
	boolean isValid();
	String[] getStringValues();
}
