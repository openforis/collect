package org.openforis.collect.relational.model;

import java.math.BigInteger;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataParentKeyColumn extends IdColumn<Node<?>> {

	private static final int NODE_ID_MAX_VALUE = 1000000;

	DataParentKeyColumn(String name) {
		super(name);
	}

	@Override
	public Object extractValue(Node<?> context) {
		Entity parent = context.getParent();
		if ( parent == null ) {
			throw new NullPointerException("Parent node");
		}
		// For attributes and entities inside single entities, recurse up to first multiple entity
		EntityDefinition parentDefn = parent.getDefinition();
		if ( !parentDefn.isMultiple() ) {
			return extractValue(parent);
		}
		Integer parentId = parent.getInternalId();
		if ( parentId == null ) {
			throw new NullPointerException("Parent node id");
		}
		Record record = context.getRecord();
		BigInteger result = BigInteger.valueOf(parentId);
		//result = parentId + recordId * NODE_ID_MAX_VALUE
		result = result.add(BigInteger.valueOf(record.getId()).multiply(BigInteger.valueOf(NODE_ID_MAX_VALUE)));
		return result;
	}
}
