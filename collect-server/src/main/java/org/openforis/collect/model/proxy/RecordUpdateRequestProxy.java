/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.Proxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordUpdateRequest;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class RecordUpdateRequestProxy<T extends RecordUpdateRequest> implements Proxy {
	
	public abstract T toUpdateRequest(CollectRecord record);
	
}
