package org.openforis.collect.metamodel.ui;

import org.openforis.collect.metamodel.ui.UIOptions.CodeAttributeLayoutType;
import org.openforis.collect.metamodel.ui.UIOptions.Orientation;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;

public class UICodeField extends UIField {

	private static final long serialVersionUID = 1L;

	private CodeAttributeLayoutType layout = CodeAttributeLayoutType.DROPDOWN;
	private Orientation itemsOrientation = Orientation.VERTICAL;
	private boolean showCode = true;
	
	<P extends UIFormContentContainer> UICodeField(P parent, int id) {
		super(parent, id);
	}
	
	public Integer getListId() {
		CodeAttributeDefinition attrDef = (CodeAttributeDefinition) getAttributeDefinition();
		CodeList list = attrDef.getList();
		return list == null ? null : list.getId();
	}
	
	public CodeAttributeLayoutType getLayout() {
		return layout;
	}
	
	public void setLayout(CodeAttributeLayoutType layout) {
		this.layout = layout;
	}

	public boolean isShowCode() {
		return showCode;
	}

	public void setShowCode(boolean showCode) {
		this.showCode = showCode;
	}

	public Orientation getItemsOrientation() {
		return itemsOrientation;
	}
	
	public void setItemsOrientation(Orientation itemsOrientation) {
		this.itemsOrientation = itemsOrientation;
	}
}
