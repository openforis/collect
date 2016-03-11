/**
 * 
 */
package org.openforis.idm.model;

import java.util.Map;

/**
 * @author S. Ricci
 *
 */
public abstract class AbstractValue implements Value {

	public abstract Map<String, Object> toMap();

	public abstract String toPrettyFormatString();
	
	public abstract String toInternalString();
	
}
