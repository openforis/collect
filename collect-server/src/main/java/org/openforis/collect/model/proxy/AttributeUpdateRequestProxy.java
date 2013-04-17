/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.AttributeUpdateRequest;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Value;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeUpdateRequestProxy extends BaseAttributeRequestProxy<AttributeUpdateRequest<?>> {
	
	private Integer nodeId;

	@Override
	public AttributeUpdateRequest<?> toUpdateRequest(CollectRecord record) {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("unchecked")
	public AttributeUpdateRequest<?> toUpdateRequest(CollectRecord record, RecordFileManager fileManager, SessionManager sessionManager) {
		AttributeUpdateRequest<Value> request = new RecordUpdateRequest.AttributeUpdateRequest<Value>();
		Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
		request.setAttribute((Attribute<?, Value>) attribute);
		if ( value != null ) {
			Value parsedValue;
			if ( attribute instanceof FileAttribute ) {
				parsedValue = parseFileAttributeValue(record, fileManager, sessionManager, nodeId, value);
			} else {
				parsedValue = parseCompositeAttributeValue(attribute.getParent(), attribute.getDefinition(), value);
			}
			request.setValue(parsedValue);
		}
		request.setSymbol(symbol);
		request.setRemarks(remarks);
		return request;
	}
	
	
	public Integer getNodeId() {
		return nodeId;
	}
	
	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}
	
}
