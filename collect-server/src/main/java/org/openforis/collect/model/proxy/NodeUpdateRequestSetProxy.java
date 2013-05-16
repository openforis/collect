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
import org.openforis.collect.remoting.service.NodeUpdateRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequestSet;

/**
 * @author S. Ricci
 *
 */
public class NodeUpdateRequestSetProxy implements Proxy {

	private List<NodeUpdateRequestProxy<?>> requests;
	private boolean autoSave;

	public NodeUpdateRequestSet toNodeUpdateOptionsSet(CollectRecord record, RecordFileManager fileManager, SessionManager sessionManager) {
		NodeUpdateRequestSet result = new NodeUpdateRequestSet();
		List<NodeUpdateRequest> convertedOptions = new ArrayList<NodeUpdateRequest>();
		for (NodeUpdateRequestProxy<?> proxy : requests) {
			NodeUpdateRequest converted;
			if ( proxy instanceof AttributeUpdateRequestProxy ) {
				converted = ((AttributeUpdateRequestProxy) proxy).toNodeUpdateOptions(record, fileManager, sessionManager);
			} else {
				converted = proxy.toNodeUpdateOptions(record);
			}
			convertedOptions.add(converted);
		}
		result.setRequests(convertedOptions);
		return result;
	}
	
	public List<NodeUpdateRequestProxy<?>> getRequests() {
		return requests;
	}
	
	public void setRequests(List<NodeUpdateRequestProxy<?>> requests) {
		this.requests = requests;
	}

	public boolean isAutoSave() {
		return autoSave;
	}

	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
	}

}
