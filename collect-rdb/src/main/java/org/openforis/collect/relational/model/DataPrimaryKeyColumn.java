package org.openforis.collect.relational.model;

import java.math.BigInteger;

import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataPrimaryKeyColumn extends IdColumn<Node<?>> implements PrimaryKeyColumn {

	private static final int NODE_ID_MAX_VALUE = 1000000;

	DataPrimaryKeyColumn(String name) {
		super(name);
	}

	@Override
	public Object extractValue(Node<?> context) {
		Integer id = context.getInternalId();
		if ( id == null ) {
			throw new NullPointerException("Node id");
		}
		Record record = context.getRecord();
		BigInteger result = BigInteger.valueOf(id);
		//result = id + recordId * NODE_ID_MAX_VALUE
		result = result.add(BigInteger.valueOf(record.getId()).multiply(BigInteger.valueOf(NODE_ID_MAX_VALUE)));
		return result;
	}
}
