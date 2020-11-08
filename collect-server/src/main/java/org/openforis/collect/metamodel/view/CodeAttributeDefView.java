package org.openforis.collect.metamodel.view;

import java.util.List;

import org.openforis.collect.designer.metamodel.AttributeType;

public class CodeAttributeDefView extends AttributeDefView {

	private int codeListId;
	private Integer parentCodeAttributeDefinitionId;

	public CodeAttributeDefView(int id, String name, String label, AttributeType type, List<String> fieldNames,
			boolean key, boolean multiple) {
		super(id, name, label, type, fieldNames, key, multiple);
	}

	public int getCodeListId() {
		return codeListId;
	}

	public void setCodeListId(int codeListId) {
		this.codeListId = codeListId;
	}
	
	public Integer getParentCodeAttributeDefinitionId() {
		return parentCodeAttributeDefinitionId;
	}

	public void setParentCodeAttributeDefinitionId(Integer parentCodeAttributeDefinitionId) {
		this.parentCodeAttributeDefinitionId = parentCodeAttributeDefinitionId;
	}

}
