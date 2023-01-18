package org.openforis.idm.testfixture;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.Value;

public class NodeBuilder {
	
	private boolean buildsAttribute;
	private String name;
	private Object value;
	private NodeBuilder[] builders;
	
	private NodeBuilder(boolean buildsAttribute, String name, Object value, NodeBuilder... builders) {
		this.buildsAttribute = buildsAttribute;
		this.name = name;
		this.value = value;
		this.builders = builders;
	}

	public static <R extends Record> R record(Survey survey, NodeBuilder... builders) {
		return record(survey, (String) null, (String) null, builders);
	}
	
	public static <R extends Record> R record(Survey survey, String rootEntityName, String versionName, NodeBuilder... builders) {
		if (rootEntityName == null) {
			List<EntityDefinition> rootEntityDefs = survey.getSchema().getRootEntityDefinitions();
			rootEntityName = rootEntityDefs.get(rootEntityDefs.size() - 1).getName();
		}
		if (versionName == null) {
			List<ModelVersion> versions = survey.getVersions();
			if (! versions.isEmpty()) {
				versionName = versions.get(versions.size() - 1).getName();
			}
		}
		@SuppressWarnings("unchecked")
		R record = (R) new CollectRecord((CollectSurvey) survey, versionName, rootEntityName);
		addChildren(record.getRootEntity(), builders);
		return record;
	}
	
	public static Entity detachedEntity(Survey survey, final String name, NodeBuilder... builders) {
		NodeBuilder builder = entity(name, builders);
		final List<EntityDefinition> defs = new ArrayList<EntityDefinition>();
		survey.getSchema().traverse(new NodeDefinitionVisitor() {
			@Override
			public void visit(NodeDefinition definition) {
				if(definition.getName().equals(name)) {
					defs.add((EntityDefinition) definition);
				}
			}
		});
		return builder.createDetachedEntity(defs.get(0));
	}
	
	public static NodeBuilder entity(String name, NodeBuilder... builders) {
		return new NodeBuilder(false, name, null, builders);
	}
	
	public static NodeBuilder attribute(String name) {
		return attribute(name, null);
	}
	
	public static NodeBuilder attribute(String name, Object value) {
		return new NodeBuilder(true, name, value);
	}
	
	private Entity createEntity(Entity parent) {
		EntityDefinition def = (EntityDefinition) parent.getDefinition().getChildDefinition(name);
		Entity entity = createDetachedEntity(def);
		return entity;
	}

	protected Entity createDetachedEntity(EntityDefinition def) {
		Entity entity = (Entity) def.createNode();
		addChildren(entity, builders);
		return entity;
	}

	private static void addChildren(Entity entity, NodeBuilder[] builders) {
		for (NodeBuilder builder : builders) {
			Node<?> child;
			if ( builder.buildsAttribute ) {
				child = builder.createAttribute(entity);
			} else {
				child = builder.createEntity(entity);
			}
			entity.add(child);
		}
	}
	
	private Attribute<?, ?> createAttribute(Entity parent) {
		AttributeDefinition def = (AttributeDefinition) parent.getDefinition().getChildDefinition(name);
		@SuppressWarnings("unchecked")
		Attribute<?, Value> attr = (Attribute<?, Value>) def.createNode();
		if ( value != null ) {
			if (value instanceof Value) {
				attr.setValue((Value) value);
			} else {
				attr.setValue(def.<Value>createValue(value.toString()));
			}
			attr.updateSummaryInfo();
		}
		return attr;
	}

}