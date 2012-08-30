/**
 * 
 */
package org.openforis.collect.designer.form;

import javax.xml.namespace.QName;

import org.openforis.idm.metamodel.NodeDefinition;

/**
 * @author S. Ricci
 *
 */
public class BooleanAttributeDefinitionFormObject extends AttributeDefinitionFormObject {
	
	private static final QName AFFIRMATIVE_ONLY_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/ui", "type");
	
	private boolean affirmativeOnly;
	
	@Override
	public void copyValues(NodeDefinition dest, String languageCode) {
		super.copyValues(dest, languageCode);
		dest.setAnnotation(AFFIRMATIVE_ONLY_ANNOTATION, Boolean.toString(affirmativeOnly));
	}
	
	@Override
	public void setValues(NodeDefinition source, String languageCode) {
		super.setValues(source, languageCode);
		String affirmativeOnlyStringValue = source.getAnnotation(AFFIRMATIVE_ONLY_ANNOTATION);
		affirmativeOnly = Boolean.parseBoolean(affirmativeOnlyStringValue);
	}

	public boolean isAffirmativeOnly() {
		return affirmativeOnly;
	}

	public void setAffirmativeOnly(boolean affirmativeOnly) {
		this.affirmativeOnly = affirmativeOnly;
	}

}
