package org.openforis.collect.io.data;

import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public interface NodeFilter {

	boolean accept(Node<?> node);

}
