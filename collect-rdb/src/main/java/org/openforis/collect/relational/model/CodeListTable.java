package org.openforis.collect.relational.model;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus
 *
 */
public class CodeListTable extends AbstractTable<CodeListItem> {
	
	private CodeList codeList;
	private CodeListTable parent;

	CodeListTable(String prefix, String baseName, CodeList codeList, CodeListTable parent) {
		super(prefix, baseName);
		this.codeList = codeList;
		this.parent = parent;
	}
	
	public CodeList getCodeList() {
		return codeList;
	}

	public CodeListTable getParent() {
		return parent;
	}
	
	public Integer getLevelIdx() {
		Integer result = null;
		if ( codeList.isHierarchical() ) {
			result = 1;
			CodeListTable cp = parent;
			while ( cp != null ) {
				result++;
				cp = cp.parent;
			}
		}
		return result;
	}
	
	@Override
	public Row extractRow(CodeListItem source) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
