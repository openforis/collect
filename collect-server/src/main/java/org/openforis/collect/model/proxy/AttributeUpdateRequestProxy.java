/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.SessionManager;
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
	public AttributeUpdateRequest<?> toAttributeUpdateRequest(CodeListManager codeListManager, RecordFileManager fileManager, 
			SessionManager sessionManager, CollectRecord record) {
		AttributeUpdateRequest<Value> opts = new NodeUpdateRequest.AttributeUpdateRequest<Value>();
		Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
		opts.setAttribute((Attribute<?, Value>) attribute);
		if ( value != null ) {
			Value parsedValue;
			if ( attribute instanceof FileAttribute ) {
				parsedValue = parseFileAttributeValue(fileManager, record, sessionManager, nodeId, value);
			} else {
				Entity parentEntity = attribute.getParent();
				String attributeName = attribute.getName();
				parsedValue = parseCompositeAttributeValue(codeListManager, parentEntity, attributeName, value);
			}
			opts.setValue(parsedValue);
		}
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
