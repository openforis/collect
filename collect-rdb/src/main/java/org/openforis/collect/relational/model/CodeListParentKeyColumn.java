package org.openforis.collect.relational.model;

import org.openforis.collect.relational.DatabaseExporterConfig;
import org.openforis.idm.metamodel.CodeListItem;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListParentKeyColumn extends IdColumn<CodeListItem> {

	CodeListParentKeyColumn(String name) {
		super(name, true);
	}

	@Override
	public Object extractValue(CodeListItem source) {
		return extractValue(DatabaseExporterConfig.createDefault(), source);
	}

	@Override
	public Object extractValue(DatabaseExporterConfig config,
			CodeListItem source) {
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
