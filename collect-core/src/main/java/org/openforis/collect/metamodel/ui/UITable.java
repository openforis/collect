package org.openforis.collect.metamodel.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class UITable extends UIModelObject implements NodeDefinitionUIComponent, UIFormComponent, UITableHeadingContainer {

	private static final long serialVersionUID = 1L;

	public enum Direction {
		BY_ROWS("byRows"), 
		BY_COLUMNS("byColumns");
		
		private String value;

		private Direction(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	private Integer entityDefinitionId;
	private EntityDefinition entityDefinition;
	private List<UITableHeadingComponent> headingComponents;
	private boolean showRowNumbers;
	private boolean countInSummaryList;
	private Direction direction;
	private int columnSpan;
	private int column;
	private int row;
	
	<P extends UIFormContentContainer> UITable(P parent, int id) {
		super(parent, id);
	}
	
	@Override
	public UIColumn createColumn() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createColumn(uiOptions.nextId());
	}
	
	@Override
	public UIColumn createColumn(int id) {
		return new UIColumn(this, id);
	}
	
	@Override
	public UIColumnGroup createColumnGroup() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createColumnGroup(uiOptions.nextId());
	}
	
	@Override
	public UIColumnGroup createColumnGroup(int id) {
		return new UIColumnGroup(this, id);
	}
	
	@Override
	public int getNodeDefinitionId() {
		return getEntityDefinitionId();
	}
	
	@Override
	public NodeDefinition getNodeDefinition() {
		return getEntityDefinition();
	}
	
	@Override
	public int indexOf(UITableHeadingComponent component) {
		return headingComponents.indexOf(component);
	}
	
	public Integer getEntityDefinitionId() {
		return entityDefinitionId;
	}

	public void setEntityDefinitionId(Integer entityDefinitionId) {
		this.entityDefinitionId = entityDefinitionId;
	}

	public EntityDefinition getEntityDefinition() {
		if ( entityDefinitionId != null && entityDefinition == null ) {
			this.entityDefinition = (EntityDefinition) getNodeDefinition(entityDefinitionId);
		}
		return entityDefinition;
	}
	
	public void setEntityDefinition(EntityDefinition entityDefinition) {
		this.entityDefinition = entityDefinition;
		this.entityDefinitionId = entityDefinition == null ? null: entityDefinition.getId();
	}
	
	public List<UITableHeadingComponent> getHeadingComponents() {
		return CollectionUtils.unmodifiableList(headingComponents);
	}
	
	public void addHeadingComponent(UITableHeadingComponent component) {
		if ( headingComponents == null ) {
			headingComponents = new ArrayList<UITableHeadingComponent>();
		}
		headingComponents.add(component);
		getUIConfiguration().attachItem(component);
	}
	
	public void removeHeadingComponent(UITableHeadingComponent component) {
		headingComponents.remove(component);
		getUIConfiguration().detachItem(component);
	}

	/**
	 * Calculate the maximum depth of the headings 
	 * @return
	 */
	public int getTotalHeadingRows() {
		return getHeadingRows().size();
	}
	
	public List<List<UITableHeadingComponent>> getHeadingRows() {
		if (headingComponents.isEmpty()) {
			return Collections.emptyList();
		}
		List<List<UITableHeadingComponent>> result = new ArrayList<List<UITableHeadingComponent>>();
		Deque<List<UITableHeadingComponent>> stack = new LinkedList<List<UITableHeadingComponent>>();
		stack.add(headingComponents);
		while(! stack.isEmpty()) {
			List<UITableHeadingComponent> currentRowComponents = stack.pop();
			result.add(currentRowComponents);
			List<UITableHeadingComponent> nextRowComponents = new ArrayList<UITableHeadingComponent>();
			for (UITableHeadingComponent currentRowComponent: currentRowComponents) {
				if (currentRowComponent instanceof UIColumnGroup) {
					nextRowComponents.addAll(((UIColumnGroup) currentRowComponent).getHeadingComponents());
				}
			}
			if (! nextRowComponents.isEmpty()) {
				stack.add(nextRowComponents);
			}
		}
		return result;
	}
	
	public List<UIColumn> getHeadingColumns() {
		List<UIColumn> columns = new ArrayList<UIColumn>();
		Deque<UITableHeadingComponent> stack = new LinkedList<UITableHeadingComponent>();
		stack.addAll(headingComponents);
		while(! stack.isEmpty()) {
			UITableHeadingComponent component = stack.pop();
			if (component instanceof UIColumn) {
				columns.add((UIColumn) component);
			} else {
				List<UITableHeadingComponent> groupComponents = new ArrayList<UITableHeadingComponent>(
						((UIColumnGroup) component).getHeadingComponents());
				Collections.reverse(groupComponents);
				
				for (UITableHeadingComponent groupComponent : groupComponents) {
					stack.push(groupComponent);
				}
			}
		}
		return columns;
	}
	
	public int getTotalHeadingColumns() {
		return getHeadingColumns().size();
	}

	/**
	 * Finds the component that will be in the specified position
	 */
	public UITableHeadingComponent findComponent(int row, int col) {
		Deque<UITableHeadingComponent> stack = new LinkedList<UITableHeadingComponent>();
		stack.addAll(headingComponents);
		while (! stack.isEmpty()) {
			UITableHeadingComponent component = stack.pop();
			if (component.getRow() <= row && (component.getRow() + component.getRowSpan()) >= row
					&& component.getCol() <= col && (component.getCol() + component.getColSpan()) >= col) {
				return component;
			}
			if (component instanceof UIColumnGroup) {
				stack.addAll(((UIColumnGroup) component).getHeadingComponents());
			}
		}
		return null;
	}
	
	public boolean isShowRowNumbers() {
		return showRowNumbers;
	}

	public void setShowRowNumbers(boolean showRowNumbers) {
		this.showRowNumbers = showRowNumbers;
	}

	public boolean isCountInSummaryList() {
		return countInSummaryList;
	}

	public void setCountInSummaryList(boolean countInSummaryList) {
		this.countInSummaryList = countInSummaryList;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	
	@Override
	public int getColumn() {
		return column;
	}
	
	@Override
	public void setColumn(int column) {
		this.column = column;
	}

	@Override
	public int getColumnSpan() {
		return columnSpan;
	}
	
	@Override
	public void setColumnSpan(int columnSpan) {
		this.columnSpan = columnSpan;
	}

	@Override
	public int getRow() {
		return row;
	}
	
	@Override
	public void setRow(int row) {
		this.row = row;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((headingComponents == null) ? 0 : headingComponents.hashCode());
		result = prime * result + (countInSummaryList ? 1231 : 1237);
		result = prime * result
				+ ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + (showRowNumbers ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UITable other = (UITable) obj;
		if (headingComponents == null) {
			if (other.headingComponents != null)
				return false;
		} else if (!headingComponents.equals(other.headingComponents))
			return false;
		if (countInSummaryList != other.countInSummaryList)
			return false;
		if (direction != other.direction)
			return false;
		if (showRowNumbers != other.showRowNumbers)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " associated to entity: " + getEntityDefinition().getPath();
	}

}
