/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.remoting.service.NodeUpdateRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.AttributeUpdateRequest;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Value;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeUpdateRequestProxy extends BaseAttributeUpdateRequestProxy<AttributeUpdateRequest<?>> {
	
	private Integer nodeId;

	@Override
	@SuppressWarnings("unchecked")
	public AttributeUpdateRequest<?> toAttributeUpdateRequest(CodeListManager codeListManager, RecordSessionManager sessionManager, 
			CollectRecord record) {
		AttributeUpdateRequest<Value> opts = new NodeUpdateRequest.AttributeUpdateRequest<Value>();
		Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
		opts.setAttribute((Attribute<?, Value>) attribute);
		Value parsedValue;
		if ( attribute instanceof FileAttribute ) {
			parsedValue = parseFileAttributeValue(sessionManager, record, nodeId, value);
		} else if ( value == null ) {
			parsedValue = null;
		} else {
			Entity parentEntity = attribute.getParent();
			String attributeName = attribute.getName();
			parsedValue = parseCompositeAttributeValue(codeListManager, parentEntity, attributeName, value);
		}
		opts.setValue(parsedValue);
		opts.setSymbol(symbol);
		return opts;
	}
	
	public Integer getNodeId() {
		return nodeId;
	}
	
	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}
	
}
