/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;

/**
 * @author M. Togna
 * 
 */
public abstract class AttributeDefinitionProxy extends NodeDefinitionProxy implements Proxy {

	protected static final QName AUTOCOMPLETE_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/ui", "autocomplete");

	private transient AttributeDefinition attributeDefinition;

	public AttributeDefinitionProxy(EntityDefinitionProxy parent, AttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}

	@ExternalizedProperty
	public boolean isKey() {
		if(this.attributeDefinition instanceof KeyAttributeDefinition) {
			return ((KeyAttributeDefinition) this.attributeDefinition).isKey(); 
		} else {
			return false;
		}
	}
	
	@ExternalizedProperty
	public boolean isDefaultValueApplicable() {
		return ! attributeDefinition.getAttributeDefaults().isEmpty();
	}
	
	@ExternalizedProperty
	public boolean isAutocomplete() {
		String autocompleteStrValue = attributeDefinition.getAnnotation(AUTOCOMPLETE_ANNOTATION);
		boolean autocomplete = StringUtils.isNotBlank(autocompleteStrValue);
		return autocomplete;
	}
	
}
