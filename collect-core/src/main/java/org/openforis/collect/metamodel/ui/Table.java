package org.openforis.collect.metamodel.ui;

import java.util.ArrayList;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class Table extends UIModelObject implements NodeDefinitionUIComponent, FormComponent, TableHeadingContainer {

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
	private List<TableHeadingComponent> headingComponents;
	private boolean showRowNumbers;
	private boolean countInSummaryList;
	private Direction direction;
	
	<P extends FormContentContainer> Table(P parent, int id) {
		super(parent, id);
	}
	
	@Override
	public Column createColumn() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createColumn(uiOptions.nextId());
	}
	
	@Override
	public Column createColumn(int id) {
		return new Column(this, id);
	}
	
	@Override
	public ColumnGroup createColumnGroup() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createColumnGroup(uiOptions.nextId());
	}
	
	@Override
	public ColumnGroup createColumnGroup(int id) {
		return new ColumnGroup(this, id);
	}
	
	@Override
	public int getNodeDefinitionId() {
		return getEntityDefinitionId();
	}
	
	@Override
	public NodeDefinition getNodeDefinition() {
		return getEntityDefinition();
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
	
	public List<TableHeadingComponent> getHeadingComponents() {
		return CollectionUtils.unmodifiableList(headingComponents);
	}
	
	public void addHeadingComponent(TableHeadingComponent component) {
		if ( headingComponents == null ) {
			headingComponents = new ArrayList<TableHeadingComponent>();
		}
		headingComponents.add(component);
		getUIConfiguration().attachItem(component);
	}
	
	public void removeHeadingComponent(TableHeadingComponent component) {
		headingComponents.remove(component);
		getUIConfiguration().detachItem(component);
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
		Table other = (Table) obj;
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
