package org.openforis.collect.metamodel.view;

import java.util.List;

import org.openforis.collect.designer.metamodel.AttributeType;

public class CodeAttributeDefView extends AttributeDefView {

	private int codeListId;
	private int codeListLevel;

	public CodeAttributeDefView(int id, String name, String label, AttributeType type, List<String> fieldNames,
			boolean key, boolean multiple, boolean showInRecordSummaryList, boolean qualifier, int codeListId, int codeListLevel) {
		super(id, name, label, type, fieldNames, key, multiple, showInRecordSummaryList, qualifier);
		this.codeListId = codeListId;
		this.codeListLevel = codeListLevel;
	}
	
	public int getCodeListId() {
		return codeListId;
	}
	
	public int getCodeListLevel() {
		return codeListLevel;
	}
	

}
