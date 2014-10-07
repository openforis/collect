package org.openforis.collect.model;

import java.util.List;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.TextValue;

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
		Record record = new Record(survey, null);
		Entity rootEntity = record.createRootEntity(rootEntityName);
		addChildren(rootEntity, builders);
		return record;
	}
	
	public static NodeBuilder entity(String name, NodeBuilder... builders) {
		return new NodeBuilder(false, name, null);
	}
	
	public static NodeBuilder attribute(String name) {
		return attribute(name, null);
	}
	
	public static NodeBuilder attribute(String name, String value) {
		return new NodeBuilder(true, name, value);
	}
	
	private Entity createEntity(Entity parent) {
		EntityDefinition def = (EntityDefinition) parent.getDefinition().getChildDefinition(name);
		Entity entity = new Entity(def);
		addChildren(entity, builders);
		parent.add(entity);
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