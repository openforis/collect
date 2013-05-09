package org.openforis.collect.relational.model;

import java.math.BigInteger;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataParentKeyColumn extends IdColumn<Node<?>> {

	DataParentKeyColumn(String name) {
		super(name);
	}

	@Override
	public Object extractValue(Node<?> source) {
		Entity parent = source.getParent();
		if ( parent == null ) {
			throw new NullPointerException("Parent node");
		}
		// For attributes and entities inside single entities, recurse up to first multiple entity
		EntityDefinition parentDefn = parent.getDefinition();
		if ( !parentDefn.isMultiple() ) {
			return extractValue(parent);
		}
		BigInteger parentId = DataPrimaryKeyColumn.getArtificialId(parent);
		return parentId;
	}
}
