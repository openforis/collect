package org.openforis.collect.relational.model;

import org.openforis.idm.model.Node;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataPrimaryKeyColumn extends IdColumn<Node<?>> implements PrimaryKeyColumn {

	DataPrimaryKeyColumn(String name) {
		super(name);
	}
	
}
