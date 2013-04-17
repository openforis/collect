/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.Proxy;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequestSet;

/**
 * @author S. Ricci
 *
 */
public class RecordUpdateRequestSetProxy implements Proxy {

	private List<RecordUpdateRequestProxy<?>> requests;
	
	public RecordUpdateRequestSet toRecordUpdateResponseSet(CollectRecord record, RecordFileManager fileManager, SessionManager sessionManager) {
		RecordUpdateRequestSet requestSet = new RecordUpdateRequestSet();
		List<RecordUpdateRequest> convertedRequests = new ArrayList<RecordUpdateRequest>();
		for (RecordUpdateRequestProxy<?> requestProxy : requests) {
			RecordUpdateRequest convertedRequest;
			if ( requestProxy instanceof AttributeUpdateRequestProxy ) {
				convertedRequest = ((AttributeUpdateRequestProxy) requestProxy).toUpdateRequest(record, fileManager, sessionManager);
			} else {
				convertedRequest = requestProxy.toUpdateRequest(record);
			}
			convertedRequests.add(convertedRequest);
		}
		requestSet.setRequests(convertedRequests);
		return requestSet;
	}
	
	public List<RecordUpdateRequestProxy<?>> getRequests() {
		return requests;
	}
	
	public void setRequests(List<RecordUpdateRequestProxy<?>> requests) {
		this.requests = requests;
	}
	
}
