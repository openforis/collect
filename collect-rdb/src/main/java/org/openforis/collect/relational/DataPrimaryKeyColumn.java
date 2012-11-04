package org.openforis.collect.relational;

import java.sql.Types;

import org.openforis.idm.model.Node;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataPrimaryKeyColumn extends AbstractColumn<Node<?>> {

	DataPrimaryKeyColumn(String name) {
		super(name, Types.INTEGER, null, false);
	}

	@Override
	public Object extractValue(Node<?> context) {
		Integer id = context.getInternalId();
		if ( id == null ) {
			throw new NullPointerException("Node id");
		}
		return id;
	}
}
