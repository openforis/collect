package org.openforis.collect.designer.form;

public abstract class EditableFormObject<T> extends FormObject<T> {

	private boolean editable;
	private boolean editingStatus;

	public EditableFormObject(boolean editable) {
		this.editable = editable;
	}
	
	@Override
	protected void reset() {
		editingStatus = false;
	}
	
	public boolean getEditable() {
		return editable;
	}
	
	public boolean getEditingStatus() {
		return editingStatus;
	}
	
	public void setEditingStatus(boolean editingStatus) {
		this.editingStatus = editingStatus;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (editable ? 1231 : 1237);
		result = prime * result + (editingStatus ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		EditableFormObject other = (EditableFormObject) obj;
		if (editable != other.editable)
			return false;
		if (editingStatus != other.editingStatus)
			return false;
		return true;
	}
	
}
