package org.openforis.collect.metamodel.view;

import java.util.List;

import org.openforis.collect.metamodel.ui.UIOptions.CodeAttributeLayoutType;
import org.openforis.collect.metamodel.ui.UIOptions.Orientation;
import org.openforis.idm.metamodel.AttributeType;

public class CodeAttributeDefView extends AttributeDefView {

	private int codeListId;
	private Integer parentCodeAttributeDefinitionId;
	private Orientation itemsOrientation;
	private boolean showCode;
	private CodeAttributeLayoutType layout;
	private boolean enumerator;
	private boolean hasQualifiableItems;

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
	
	public Orientation getItemsOrientation() {
		return itemsOrientation;
	}
	
	public void setItemsOrientation(Orientation itemsOrientation) {
		this.itemsOrientation = itemsOrientation;
	}
	
	public boolean isShowCode() {
		return showCode;
	}
	
	public void setShowCode(boolean showCode) {
		this.showCode = showCode;
	}
	
	public CodeAttributeLayoutType getLayout() {
		return layout;
	}
	
	public void setLayout(CodeAttributeLayoutType layout) {
		this.layout = layout;
	}

	public boolean isEnumerator() {
		return enumerator;
	}
	
	public void setEnumerator(boolean enumerator) {
		this.enumerator = enumerator;
	}
	
	public boolean isHasQualifiableItems() {
		return hasQualifiableItems;
	}

	public void setHasQualifiableItems(boolean hasQualifiableItems) {
		this.hasQualifiableItems = hasQualifiableItems;
	}
}
