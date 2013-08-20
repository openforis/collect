/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.Proxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.remoting.service.NodeUpdateRequest;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class NodeUpdateRequestProxy<T extends NodeUpdateRequest> implements Proxy {
	
	public abstract T toNodeUpdateRequest(CollectRecord record);
	
}
