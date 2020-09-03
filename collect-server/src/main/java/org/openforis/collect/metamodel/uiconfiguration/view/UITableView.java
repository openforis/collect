package org.openforis.collect.metamodel.uiconfiguration.view;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.ui.UITable;
import org.openforis.collect.metamodel.ui.UITable.Direction;
import org.openforis.collect.metamodel.ui.UITableHeadingComponent;
import org.openforis.collect.metamodel.view.ViewContext;

public class UITableView extends UIModelObjectView<UITable> implements UITabComponentView<UITable> {

	public UITableView(UITable uiObject, ViewContext context) {
		super(uiObject, context);
	}
	
	@Override
	public String getType() {
		return "TABLE";
	}
	
	public int getEntityDefinitionId() {
		return uiObject.getEntityDefinitionId();
	}
	
	public List<UITableHeadingComponentView<?>> getHeadingComponents() {
		return UITableHeadingComponentView.fromObjects(uiObject.getHeadingComponents(), context);
	}
	
	public List<List<UITableHeadingComponentView<?>>> getHeadingRows() {
		List<List<UITableHeadingComponentView<?>>> rowViews = new ArrayList<List<UITableHeadingComponentView<?>>>();
		List<List<UITableHeadingComponent>> rows = uiObject.getHeadingRows();
		for (List<UITableHeadingComponent> row : rows) {
			List<UITableHeadingComponentView<?>> rowView = UITableHeadingComponentView.fromObjects(row, context);
			rowViews.add(rowView);
		}
		return rowViews;
	}

	public int getTotalHeadingRows() {
		return uiObject.getTotalHeadingRows();
	}
	
	public List<UIColumnView> getHeadingColumns() {
		return UITableHeadingComponentView.fromObjects(uiObject.getHeadingColumns(), context);
	}
	
	public int getTotalHeadingColumns() {
		return uiObject.getTotalHeadingColumns();
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