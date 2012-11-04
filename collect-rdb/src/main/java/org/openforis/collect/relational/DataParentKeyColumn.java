package org.openforis.collect.relational;

import java.sql.Types;

import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataParentKeyColumn extends AbstractColumn<Node<?>> {

	DataParentKeyColumn(String name) {
		super(name, Types.INTEGER, null, false);
	}

	@Override
	public Object extractValue(Node<?> context) {
		Entity parent = context.getParent();
		if ( parent == null ) {
			throw new NullPointerException("Parent node");
		}
		Integer id = parent.getInternalId();
		if ( id == null ) {
			throw new NullPointerException("Parent node id");
		}
		return id;
	}
}
