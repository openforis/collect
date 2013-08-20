/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.Proxy;
import org.openforis.collect.manager.CodeListManager;
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

	public NodeUpdateRequestSet toNodeUpdateRequestSet(CodeListManager codeListManager, RecordFileManager fileManager,
			SessionManager sessionManager, CollectRecord record) {
		NodeUpdateRequestSet result = new NodeUpdateRequestSet();
		List<NodeUpdateRequest> convertedOptions = new ArrayList<NodeUpdateRequest>();
		for (NodeUpdateRequestProxy<?> proxy : requests) {
			NodeUpdateRequest converted;
			if ( proxy instanceof BaseAttributeUpdateRequestProxy<?> ) {
				converted = ((BaseAttributeUpdateRequestProxy<?>) proxy).toAttributeUpdateRequest(codeListManager, fileManager, sessionManager, record);
			} else {
				converted = proxy.toNodeUpdateRequest(record);
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
