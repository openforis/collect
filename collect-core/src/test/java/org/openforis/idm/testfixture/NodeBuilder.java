package org.openforis.idm.testfixture;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.*;

public class NodeBuilder {
	
	private boolean buildsAttribute;
	private String name;
	private String value;
	private NodeBuilder[] builders;
	
	private NodeBuilder(boolean buildsAttribute, String name, String value, NodeBuilder... builders) {
		this.buildsAttribute = buildsAttribute;
		this.name = name;
		this.value = value;
		this.builders = builders;
	}

	public static Record record(Survey survey, NodeBuilder... builders) {
		List<EntityDefinition> rootEntityDefs = survey.getSchema().getRootEntityDefinitions();
		String rootEntityName = rootEntityDefs.get(rootEntityDefs.size() - 1).getName();
		Record record = survey.createRecord();
		Entity rootEntity = record.createRootEntity(rootEntityName);
		addChildren(rootEntity, builders);
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
	
	public static NodeBuilder attribute(String name, String value) {
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
		NodeDefinition def = parent.getDefinition().getChildDefinition(name);
		@SuppressWarnings("unchecked")
		Attribute<?, TextValue> attr = (Attribute<?, TextValue>) def.createNode();
		if ( value != null ) {
			attr.setValue(new TextValue(value));
		}
		return attr;
	}

}