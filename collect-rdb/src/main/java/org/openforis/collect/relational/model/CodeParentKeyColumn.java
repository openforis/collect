package org.openforis.collect.relational.model;

import org.openforis.idm.metamodel.CodeListItem;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeParentKeyColumn extends IdColumn<CodeListItem> {

	CodeParentKeyColumn(String name) {
		super(name, true);
	}

}
