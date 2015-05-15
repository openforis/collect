package org.openforis.collect.metamodel.ui;

import java.util.ArrayList;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class UIFormContentContainer extends UIModelObject {

	private static final long serialVersionUID = 1L;

	private List<UIFormComponent> children;
	private List<UIForm> forms;
	
	UIFormContentContainer(UIModelObject parent, int id) {
		super(parent, id);
	}
	
	public List<UIFormComponent> getChildren() {
		return CollectionUtils.unmodifiableList(children);
	}
	
	public void addChild(UIFormComponent child) {
		if ( children == null ) {
			children = new ArrayList<UIFormComponent>();
		}
		children.add(child);
		getUIConfiguration().attachItem(child);
	}
	
	public void removeChild(UIFormComponent child) {
		children.remove(child);
		getUIConfiguration().detachItem(child);
	}

	public UIFormSection createFormSection() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createFormSection(uiOptions.nextId());
	}
	
	public UIFormSection createFormSection(int id) {
		return new UIFormSection(this, id);
	}
	
	public UIField createField() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createField(uiOptions.nextId());
	}
	
	public UIField createField(int id) {
		return new UIField(this, id);
	}

	public UITable createTable() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createTable(uiOptions.nextId());
	}
	
	public UITable createTable(int id) {
		return new UITable(this, id);
	}
	
	public UIForm createForm() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createForm(uiOptions.nextId());
	}
	
	public UIForm createForm(int id) {
		return new UIForm(this, id);
	}

	public List<UIForm> getForms() {
		return CollectionUtils.unmodifiableList(forms);
	}
	
	public void addForm(UIForm form) {
		if ( forms == null ) {
			forms = new ArrayList<UIForm>();
		}
		forms.add(form);
		getUIConfiguration().attachItem(form);
	}
	
	public void removeForm(UIForm form) {
		forms.remove(form);
		getUIConfiguration().detachItem(form);
	}
}
