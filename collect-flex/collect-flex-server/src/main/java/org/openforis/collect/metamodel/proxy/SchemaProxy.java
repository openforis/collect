/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
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

}
