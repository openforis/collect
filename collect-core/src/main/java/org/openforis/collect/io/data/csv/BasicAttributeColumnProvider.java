/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.Entity;

/**
 * @author S. Ricci
 *
 */
public abstract class BasicAttributeColumnProvider<T extends AttributeDefinition> extends BasicColumnProvider {

	protected T attributeDefinition;
	private List<Column> columns;

	public BasicAttributeColumnProvider(CSVDataExportParameters config, T attrDefn) {
		super(config);
		this.attributeDefinition = attrDefn;
	}

	protected int getMaxAttributeValues() {
		if (attributeDefinition.isMultiple()) {
			return ObjectUtils.defaultIfNull(attributeDefinition.getFixedMaxCount(), getConfig().getMaxMultipleAttributeValues());
		} else {
			return 1;
		}
	}
	
	@Override
	public List<Column> getColumns() {
		if (columns == null) {
			columns = generateColumns();
		}
		return columns;
	}

	protected List<Column> generateColumns() {
		if (attributeDefinition.isMultiple()) {
			int maxAttrValues = getMaxAttributeValues();
			int numberOfColumnsPerAttribute = getNumberOfColumnsPerAttribute();
			List<Column> columns = new ArrayList<Column>(maxAttrValues * numberOfColumnsPerAttribute);
			for (int i = 0; i < maxAttrValues; i++) {
				columns.addAll(generateAttributeColumns(i));
			}
			return columns;
		} else {
			return generateSingleAttributeColumns();
		}
	}
	
	@Override
	protected String generateHeadingPrefix() {
		return ColumnProviders.generateHeadingPrefix(attributeDefinition, config);
	}

	protected abstract int getNumberOfColumnsPerAttribute();

	protected abstract List<Column> generateSingleAttributeColumns();

	protected abstract List<Column> generateAttributeColumns(int i);
	
	protected String generateAttributePositionSuffix(int attributeIdx) {
		return attributeDefinition.isMultiple() ? "[" + (attributeIdx + 1) + "]": "";
	}
	
	/**
	 * Attribute definition can be inside nested single entities inside the axis or it can be in an ancestor entity.
	 * This method will look for the nearest parent entity for attributes to extract value for.
	 */
	protected Entity getAttributeParentEntity(Entity axis) {
		EntityDefinition axisDef = axis.getDefinition();
		List<EntityDefinition> ancestorEntityDefinitions = attributeDefinition.getAncestorEntityDefinitionsInReverseOrder();
		int indexOfAxis = ancestorEntityDefinitions.indexOf(axisDef);
		if (indexOfAxis > 0) {
			//attribute is inside one or more single entities
			Entity nearestParentEntity = axis;
			if (indexOfAxis + 1 < ancestorEntityDefinitions.size()) {
				List<EntityDefinition> relativeSingleEntityDefs = ancestorEntityDefinitions.subList(indexOfAxis + 1, ancestorEntityDefinitions.size());
				for (EntityDefinition parentSingleEntityDef : relativeSingleEntityDefs) {
					nearestParentEntity = (Entity) nearestParentEntity.getChild(parentSingleEntityDef);
				}
			}
			return nearestParentEntity;
		} else {
			//attribute is in an ancestor entity
			EntityDefinition attrParentEntityDef = attributeDefinition.getParentEntityDefinition();
			Entity currentEntity = axis;
			while (currentEntity.getDefinition() != attrParentEntityDef && !currentEntity.isRoot()) {
				currentEntity = currentEntity.getParent();
			}
			return currentEntity;
		}
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(null)
			.append("Attribute", attributeDefinition.getName())
			.append("Columns", getColumns())
			.build();
	}
}
