package org.openforis.collect.relational.model;

import org.openforis.collect.relational.DatabaseExporterConfig;
import org.openforis.idm.metamodel.CodeListItem;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus
 *
 */
public class CodeListPrimaryKeyColumn extends IdColumn<CodeListItem> implements PrimaryKeyColumn {

	CodeListPrimaryKeyColumn(String name) {
		super(name);
	}

	@Override
	public Object extractValue(CodeListItem source) {
		return extractValue(DatabaseExporterConfig.createDefault(), source);
	}
	
	@Override
	public Object extractValue(DatabaseExporterConfig config,
			CodeListItem source) {
		Integer id = source.getId();
		if ( id == null ) {
			throw new NullPointerException("Node id");
		}
		return id;
	}
	
}
