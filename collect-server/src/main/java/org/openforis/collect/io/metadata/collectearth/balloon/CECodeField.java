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
	private Map<String, List<CodeListItem>> codeItemsByParentCode = new HashMap<String, List<CodeListItem>>();

	public CECodeField(String htmlParameterName, String name, String label, CEFieldType type, boolean multiple, boolean key, Map<String, List<CodeListItem>> codeItemsByParentCode, String parentName) {
		super(htmlParameterName, name, label, multiple, type, key);
		this.codeItemsByParentCode = codeItemsByParentCode;
		this.parentName = parentName;
	}
	
	public Map<String, List<CodeListItem>> getCodeItemsByParentCode() {
		return codeItemsByParentCode;
	}
	
	public String getParentName() {
		return parentName;
	}
	
}