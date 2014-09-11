/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;

/**
 * @author M. Togna
 * 
 */
public class SchemaProxy implements Proxy {

	private transient Schema schema;

	public SchemaProxy(Schema schema) {
		super();
		this.schema = schema;
	}

	@ExternalizedProperty
	public List<NodeDefinitionProxy> getRootEntityDefinitions() {
		return NodeDefinitionProxy.fromList(null, schema.getRootEntityDefinitions());
	}

	@ExternalizedProperty
	public Map<Integer, List<Integer>> getKeyAttributeDefinitionIdsByRootEntityId() {
		Map<Integer, List<Integer>> result = new HashMap<Integer, List<Integer>>();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		for (EntityDefinition rootEntity : rootEntityDefinitions) {
			List<Integer> keyDefnIds = new ArrayList<Integer>();
			List<AttributeDefinition> keyDefns = rootEntity.getKeyAttributeDefinitions();
			for (AttributeDefinition keyDefn : keyDefns) {
				keyDefnIds.add(keyDefn.getId());
			}
			result.put(rootEntity.getId(), keyDefnIds );
		}
		return result;
	}
}
