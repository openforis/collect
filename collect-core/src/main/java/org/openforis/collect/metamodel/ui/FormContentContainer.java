package org.openforis.collect.metamodel.ui;

import java.util.ArrayList;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class FormContentContainer extends UIModelObject {

	private static final long serialVersionUID = 1L;

	private List<FormComponent> children;
	private List<Form> forms;
	
	FormContentContainer(UIModelObject parent, int id) {
		super(parent, id);
	}
	
	public List<FormComponent> getChildren() {
		return CollectionUtils.unmodifiableList(children);
	}
	
	public void addChild(FormComponent child) {
		if ( children == null ) {
			children = new ArrayList<FormComponent>();
		}
		children.add(child);
		getUIConfiguration().attachItem(child);
	}
	
	public void removeChild(FormComponent child) {
		children.remove(child);
		getUIConfiguration().detachItem(child);
	}

	public FormSection createFormSection() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createFormSection(uiOptions.nextId());
	}
	
	public FormSection createFormSection(int id) {
		return new FormSection(this, id);
	}
	
	public Field createField() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createField(uiOptions.nextId());
	}
	
	public Field createField(int id) {
		return new Field(this, id);
	}

	public Table createTable() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createTable(uiOptions.nextId());
	}
	
	public Table createTable(int id) {
		return new Table(this, id);
	}
	
	public Form createForm() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createForm(uiOptions.nextId());
	}
	
	public Form createForm(int id) {
		return new Form(this, id);
	}

	public List<Form> getForms() {
		return CollectionUtils.unmodifiableList(forms);
	}
	
	public void addForm(Form form) {
		if ( forms == null ) {
			forms = new ArrayList<Form>();
		}
		forms.add(form);
		getUIConfiguration().attachItem(form);
	}
	
	public void removeForm(Form form) {
		forms.remove(form);
		getUIConfiguration().detachItem(form);
	}
}
