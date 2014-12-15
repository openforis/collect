/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.AttributeDefinition.FieldLabel;

/**
 * @author S. Ricci
 * 
 */
public class FieldLabelProxy extends TypedLanguageSpecificTextProxy<String, String> {

	static List<FieldLabelProxy> fromFieldLabelList(List<FieldLabel> labels) {
		List<FieldLabelProxy> proxies = new ArrayList<FieldLabelProxy>();
		if (labels != null) {
			for (FieldLabel l : labels) {
				proxies.add(new FieldLabelProxy(l));
			}
		}
		return proxies;
	}

	public FieldLabelProxy(FieldLabel nodeLabel) {
		super(nodeLabel);
	}
	
	@ExternalizedProperty
	public String getType() {
		return getTypeInternal();
	}
	
}
