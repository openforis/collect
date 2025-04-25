/**
 * 
 */
package org.openforis.collect.metamodel.ui;

import java.util.Collections;
import java.util.List;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.LanguageSpecificTextMap;

/**
 * @author S. Ricci
 *
 */
public class UIForm extends UIFormContentContainer {

	private static final long serialVersionUID = 1L;

	private LanguageSpecificTextMap labels;
	
	public <P extends UIFormContentContainer> UIForm(P parent, int id) {
		super(parent, id);
	}

	public List<LanguageSpecificText> getLabels() {
		if ( labels == null ) {
			return Collections.emptyList();
		} else {
			return labels.values();
		}
	}
	
	public String getLabel(String language, String defaultLanguage) {
		return labels == null ? null: labels.getText(language, defaultLanguage);
	}
	
	public void addLabel(LanguageSpecificText label) {
		if ( labels == null ) {
			labels = new LanguageSpecificTextMap();
		}
		labels.add(label);
	}

	public void setLabel(String language, String text) {
		if ( labels == null ) {
			labels = new LanguageSpecificTextMap();
		}
		labels.setText(language, text);
	}
	
	public void removeLabel(String language) {
		labels.remove(language);
	}
	
	public boolean isMultipleEntityForm() {
		EntityDefinition entityDefinition = getMultipleEntityDefinition();
		return entityDefinition != null && entityDefinition.isMultiple();
	}

	public EntityDefinition getMultipleEntityDefinition() {
		UIFormComponent firstChild = getFirstChild();
		return firstChild != null && firstChild instanceof UIFormSection
				? ((UIFormSection) firstChild).getEntityDefinition()
				: null;
	}

	private UIFormComponent getFirstChild() {
		return getChildren().size() == 1 ? getChildren().get(0) : null;
	}
}
