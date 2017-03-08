/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient CodeAttributeDefinition attributeDefinition;

	public CodeAttributeDefinitionProxy(EntityDefinitionProxy parent, CodeAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefinition = attributeDefinition;
	}

	@ExternalizedProperty
	public String getListName() {
		return attributeDefinition.getListName();
	}

	@ExternalizedProperty
	public boolean isAllowUnlisted() {
		return attributeDefinition.isAllowUnlisted();
	}

	@ExternalizedProperty
	public boolean isExternal() {
		CodeList list = attributeDefinition.getList();
		return list != null && list.isExternal();
	}
	
	@ExternalizedProperty
	public boolean isAllowValuesSorting() {
		return attributeDefinition.isAllowValuesSorting();
	}

	@ExternalizedProperty
	public String getLayoutType() {
		UIOptions uiOptions = getUIOptions();
		return uiOptions.getLayoutType(attributeDefinition).toString();
	}
	
	@ExternalizedProperty
	public String getLayoutDirection() {
		UIOptions uiOptions = getUIOptions();
		return uiOptions.getLayoutDirection(attributeDefinition).name();
	}
	
	@ExternalizedProperty
	public boolean isShowAllowedValuesPreview() {
		UIOptions uiOptions = getUIOptions();
		return uiOptions.getShowAllowedValuesPreviewValue(attributeDefinition);
	}

	@ExternalizedProperty
	public boolean isShowCode() {
		UIOptions uiOptions = getUIOptions();
		return uiOptions.getShowCode(attributeDefinition);
	}

}
