package org.openforis.collect.relational.model;

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

	DataParentKeyColumn(String name) {
		super(name, Types.INTEGER, "integer", null, false);
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
		return record.getId() * 1000000 + parentId;
	}
}
