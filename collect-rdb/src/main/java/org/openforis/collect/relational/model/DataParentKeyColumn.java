package org.openforis.collect.relational.model;

import java.math.BigInteger;
import java.sql.Types;

import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataParentKeyColumn extends AbstractColumn<Node<?>> {

	private static final int NODE_ID_MAX_VALUE = 1000000;

	DataParentKeyColumn(String name) {
		super(name, Types.BIGINT, "bigint", null, false);
	}

	@Override
	public Object extractValue(Node<?> context) {
		Entity parent = context.getParent();
		if ( parent == null ) {
			throw new NullPointerException("Parent node");
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
