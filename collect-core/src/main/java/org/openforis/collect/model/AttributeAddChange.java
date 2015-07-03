package org.openforis.collect.model;

import org.openforis.idm.model.Attribute;

/**
 * Change related to an Attribute that have just been added to the record.
 * 
 * @author S. Ricci
 *
 */
public class AttributeAddChange extends AttributeChange implements NodeAddChange {
	
	public AttributeAddChange(Integer recordId, Integer parentId, Attribute<?, ?> node) {
		super(recordId, parentId, node);
	}
	
}