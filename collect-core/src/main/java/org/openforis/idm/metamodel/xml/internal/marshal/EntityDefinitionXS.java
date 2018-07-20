package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.ENTITY;
import static org.openforis.idm.metamodel.xml.IdmlConstants.GENERATOR_EXPRESSION;
import static org.openforis.idm.metamodel.xml.IdmlConstants.VIRTUAL;

import java.io.IOException;
import java.util.List;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;

/**
 * 
 * @author G. Miceli
 * 
 */
public class EntityDefinitionXS extends NodeDefinitionXS<EntityDefinition, Schema> {
	public EntityDefinitionXS() {
		super(ENTITY);
		addChildMarshallers(new NodeDefinitionXSDelegator(this));
	}
	
	/**
	 * Only used by SchemaXS.  NodeDefinition finds children on its own
	 */
	@Override
	protected void marshalInstances(Schema schema) throws IOException {
		List<EntityDefinition> roots = schema.getRootEntityDefinitions();
		marshal(roots);
	}
	
	@Override
	protected void attributes(EntityDefinition defn) throws IOException {
		super.attributes(defn);
		attribute(VIRTUAL, defn.isVirtual(), false);
		attribute(GENERATOR_EXPRESSION, defn.getGeneratorExpression());
	}
}
