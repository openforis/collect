package org.openforis.collect.metamodel.uiconfiguration.view;

import java.util.List;

import org.openforis.collect.metamodel.ui.UITable;
import org.openforis.collect.metamodel.ui.UITable.Direction;

public class UITableView extends UIModelObjectView<UITable> implements UITabComponentView<UITable> {

	public UITableView(UITable uiObject) {
		super(uiObject);
	}
	
	@Override
	public String getType() {
		return "TABLE";
	}
	
	public int getEntityDefinitionId() {
		return uiObject.getEntityDefinitionId();
	}
	
	public List<UITableHeadingComponentView<?>> getHeadingComponents() {
		return UITableHeadingComponentView.fromObjects(uiObject.getHeadingComponents());
	}
	
	public boolean isShowRowNumbers() {
		return uiObject.isShowRowNumbers();
	}
	
	public boolean isCountInSummaryList() {
		return uiObject.isCountInSummaryList();
	}
	
	public Direction getDirection() {
		return uiObject.getDirection();
	}
	
	@Override
	public int getColumn() {
		return uiObject.getColumn();
	}
	
	@Override
	public int getColumnSpan() {
		return uiObject.getColumnSpan();
	}
	
	@Override
	public int getRow() {
		return uiObject.getRow();
	}

}