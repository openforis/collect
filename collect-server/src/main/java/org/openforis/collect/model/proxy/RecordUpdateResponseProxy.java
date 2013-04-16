/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.Proxy;
import org.openforis.collect.model.RecordUpdateResponse;
import org.openforis.collect.model.RecordUpdateResponse.AddAttributeResponse;
import org.openforis.collect.model.RecordUpdateResponse.AddEntityResponse;
import org.openforis.collect.model.RecordUpdateResponse.AttributeUpdateResponse;
import org.openforis.collect.model.RecordUpdateResponse.DeleteNodeResponse;
import org.openforis.collect.model.RecordUpdateResponse.EntityUpdateResponse;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * @author S. Ricci
 *
 */
public abstract class RecordUpdateResponseProxy<T extends RecordUpdateResponse> implements Proxy {

	protected transient MessageContextHolder messageContextHolder;
	protected transient T response;

	public RecordUpdateResponseProxy(MessageContextHolder messageContextHolder, T response) {
		super();
		this.response = response;
		this.messageContextHolder = messageContextHolder;
	}

	public static List<RecordUpdateResponseProxy<?>> fromList(MessageContextHolder messageContextHolder, List<RecordUpdateResponse> items) {
		List<RecordUpdateResponseProxy<?>> result = new ArrayList<RecordUpdateResponseProxy<?>>();
		if ( items != null ) {
			for (RecordUpdateResponse item : items) {
				RecordUpdateResponseProxy<?> proxy;
				if ( item instanceof AddAttributeResponse ) {
					proxy = new AddAttributeResponseProxy(messageContextHolder, (AddAttributeResponse) item);
				} else if ( item instanceof AddEntityResponse ) {
					proxy = new AddEntityResponseProxy(messageContextHolder, (AddEntityResponse) item);
				} else if ( item instanceof AttributeUpdateResponse ) {
					proxy = new AttributeUpdateResponseProxy(messageContextHolder, (AttributeUpdateResponse) item);
				} else if ( item instanceof EntityUpdateResponse ) {
					proxy = new EntityUpdateResponseProxy(messageContextHolder, (EntityUpdateResponse) item);
				} else if ( item instanceof DeleteNodeResponse ) {
					proxy = new DeleteNodeResponseProxy(messageContextHolder, (DeleteNodeResponse) item);
				} else {
					throw new IllegalArgumentException("RecordUpdateResponse not supported: " + item.getClass().getSimpleName());
				}
				result.add(proxy);
			}
		}
		return result;
	}
	
}
