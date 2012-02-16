/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * @author M. Togna
 * 
 */
public class EntityDefinitionProxy extends NodeDefinitionProxy {

	private transient EntityDefinition entityDefinition;

	public EntityDefinitionProxy(EntityDefinitionProxy parent, EntityDefinition entityDefinition) {
		super(parent, entityDefinition);
		this.entityDefinition = entityDefinition;
	}

	static List<EntityDefinitionProxy> fromList(EntityDefinitionProxy parent, List<EntityDefinition> list) {
		List<EntityDefinitionProxy> proxies = new ArrayList<EntityDefinitionProxy>();
		if (list != null) {
			for (EntityDefinition e : list) {
				proxies.add(new EntityDefinitionProxy(parent, e));
			}
		}
		return proxies;
	}

	@ExternalizedProperty
	public List<NodeDefinitionProxy> getChildDefinitions() {
		return NodeDefinitionProxy.fromList(this, entityDefinition.getChildDefinitions());
	}

	@ExternalizedProperty
	public boolean isCountInSummaryList() {
		QName countInSummaryListAnnotation = new QName("http://www.openforis.org/collect/3.0/collect", "count");
		String annotation = entityDefinition.getAnnotation(countInSummaryListAnnotation);
		return annotation != null && Boolean.parseBoolean(annotation);
	}

	@ExternalizedProperty
	public boolean isEnumerated() {
		return entityDefinition.isMultiple() && hasEnumeratingCodeListAttribute();
	}
	
	private boolean hasEnumeratingCodeListAttribute() {
		List<NodeDefinition> childDefinitions = entityDefinition.getChildDefinitions();
		for (NodeDefinition nodeDef : childDefinitions) {
			if(nodeDef instanceof CodeAttributeDefinition) {
				CodeAttributeDefinition codeDef = (CodeAttributeDefinition) nodeDef;
				if(codeDef.isKey() && codeDef.getList() != null) {
					return true;
				}
			}
		}
		return false;
	}
}
