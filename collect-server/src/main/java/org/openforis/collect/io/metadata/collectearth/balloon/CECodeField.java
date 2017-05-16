package org.openforis.collect.io.metadata.collectearth.balloon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.idm.metamodel.CodeListItem;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
class CECodeField extends CEField {
	
//	private CodeList listName;
	
	private String parentName;
	private Map<Integer, List<CodeListItem>> codeItemsByParentId = new HashMap<Integer, List<CodeListItem>>();

	public CECodeField(String htmlParameterName, String name, String label, String tooltip, CEFieldType type, boolean multiple, 
			boolean key, Map<Integer, List<CodeListItem>> codeItemsByParentCode, String parentName) {
		super(htmlParameterName, name, label, tooltip, multiple, type, key);
		this.codeItemsByParentId = codeItemsByParentCode;
		this.parentName = parentName;
	}
	
	public Map<Integer, List<CodeListItem>> getCodeItemsByParentId() {
		return codeItemsByParentId;
	}
	
	public String getParentName() {
		return parentName;
	}
	
}