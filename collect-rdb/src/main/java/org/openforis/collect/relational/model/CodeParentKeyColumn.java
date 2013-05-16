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

	@Override
	public Object extractValue(CodeListItem source) {
		CodeListItem parent = source.getParentItem();
		if ( parent == null ) {
			throw new NullPointerException("Parent code item");
		}
		Integer parentId = parent.getId();
		if ( parentId == null ) {
			throw new NullPointerException("Parent code item id");
		}
		return parentId;
	}
	
}
