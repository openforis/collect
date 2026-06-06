package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.io.data.csv.columnProviders.AutomaticColumnProvider;
import org.openforis.collect.io.data.csv.columnProviders.ColumnProvider;
import org.openforis.collect.io.data.csv.columnProviders.ColumnProviderChain;
import org.openforis.collect.io.data.csv.columnProviders.ColumnProviders;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author S. Ricci
 */
public class MultipleEntityColumnProvider extends ColumnProviderChain {
	
	private static int DEFAULT_MAX_ITEMS = 10;
	

	public MultipleEntityColumnProvider(CSVDataExportParameters config, EntityDefinition defn) {
		super(config, defn, createProviders(config, defn));
	}

	private static List<ColumnProvider> createProviders(CSVDataExportParameters config, EntityDefinition defn) {
		List<ColumnProvider> providers = new ArrayList<ColumnProvider>();
		List<AttributeDefinition> keyDefs = defn.getKeyAttributeDefinitions();
		int maxEntities = defn.getFixedMaxCount() == null ? DEFAULT_MAX_ITEMS: defn.getFixedMaxCount();
		for (int entityIndex = 0; entityIndex < maxEntities; entityIndex ++) {
			for (AttributeDefinition keyDef : keyDefs) {
				String keyName = keyDef.getName();
				MultipleEntityKeyColumnProvider p = new MultipleEntityKeyColumnProvider(config, defn, entityIndex, keyName);
				providers.add(p);
			}
		}
		return providers;
	}
	
	@Override
	protected String generateHeadingPrefix() {
		if (entityDefinition == null) {
			throw new IllegalStateException("Entity definition not specified for multiple entity column provider");
		}
		return ColumnProviders.generateHeadingPrefix(entityDefinition, config);
	}

	private static class MultipleEntityKeyColumnProvider extends AutomaticColumnProvider {

		private String keyName;
		private int entityIndex;

		public MultipleEntityKeyColumnProvider(CSVDataExportParameters config, EntityDefinition defn, int entityIndex, String keyName) {
			super(config, "[" + (entityIndex + 1) + "]_", defn);
			this.entityIndex = entityIndex;
			this.keyName = keyName;
		}
		
		@Override
		public List<Object> extractValues(Node<?> axis) {
			Entity parentEntity = (Entity) axis;
			List<Node<?>> children = parentEntity.getChildren(entityDefinition);
			Node<?> childEntity = entityIndex < children.size() ? children.get(entityIndex) : null;
			if (childEntity == null) {
				// not found; return empty array
				return super.emptyValues();
			}
			return super.extractValues(childEntity);
		}
	}
}
