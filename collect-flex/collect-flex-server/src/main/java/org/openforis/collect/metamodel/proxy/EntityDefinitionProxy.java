/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author M. Togna
 * 
 */
public class EntityDefinitionProxy extends NodeDefinitionProxy {

	private transient EntityDefinition entityDefinition;

	public EntityDefinitionProxy(EntityDefinition entityDefinition) {
		super(entityDefinition);
		this.entityDefinition = entityDefinition;
	}

	static List<EntityDefinitionProxy> fromList(List<EntityDefinition> list) {
		List<EntityDefinitionProxy> proxies = new ArrayList<EntityDefinitionProxy>();
		if (list != null) {
			for (EntityDefinition e : list) {
				proxies.add(new EntityDefinitionProxy(e));
			}
		}
		return proxies;
	}

	@ExternalizedProperty
	public List<NodeDefinitionProxy> getChildDefinitions() {
		return NodeDefinitionProxy.fromList(entityDefinition.getChildDefinitions());
	}

}
