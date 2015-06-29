package org.openforis.collect.relational.model;

import org.openforis.idm.metamodel.CodeListItem;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus
 *
 */
public class CodePrimaryKeyColumn extends IdColumn<CodeListItem> implements PrimaryKeyColumn {

	CodePrimaryKeyColumn(String name) {
		super(name);
	}

}
