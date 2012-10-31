/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 *
 */
public class EntityDefinitionFormObject<T extends EntityDefinition> extends NodeDefinitionFormObject<T> {

	//layout
	private String layoutType;

	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		UIOptions uiOptions = getUIOptions(dest);
		Layout layout = Layout.valueOf(layoutType);
		uiOptions.setLayout(dest, layout);
	}
	
	@Override
	public void loadFrom(T source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		UIOptions uiOptions = getUIOptions(source);
		Layout layout = uiOptions.getLayout(source);
		layoutType = layout.name();
	}

	@Override
	protected void reset() {
		super.reset();
		layoutType = null;
	}
	
	public String getLayoutType() {
		return layoutType;
	}

	public void setLayoutType(String layoutType) {
		this.layoutType = layoutType;
	}

}
