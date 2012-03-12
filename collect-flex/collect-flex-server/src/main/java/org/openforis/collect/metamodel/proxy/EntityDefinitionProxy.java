/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class EntityDefinitionProxy extends NodeDefinitionProxy {

	private static QName countInSummaryListAnnotation = new QName("http://www.openforis.org/collect/3.0/collect", "count");
	private static QName layoutAnnotation = new QName("http://www.openforis.org/collect/3.0/ui", "layout");

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
		String annotation = entityDefinition.getAnnotation(countInSummaryListAnnotation);
		return annotation != null && Boolean.parseBoolean(annotation);
	}

	@ExternalizedProperty
	public boolean isEnumerable() {
		return entityDefinition.isMultiple() && entityDefinition.isEnumerable();
	}

	@ExternalizedProperty
	public String getLayout() {
		String result = entityDefinition.getAnnotation(layoutAnnotation);
		if(StringUtils.isNotBlank(result)) {
			return result;
		} else if(isMultiple()) {
			return "table";
		} else {
			if(parent != null) {
				return parent.getLayout();
			} else {
				return "form";
			}
		}
	}
	
}
