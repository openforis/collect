package org.openforis.collect.designer.form;

import org.openforis.collect.utils.SurveyObjects;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition.Attribute;

public class AttributeFormObject extends EditableFormObject<Attribute> {

	private String name;
	private int index;

	public AttributeFormObject(boolean editable, int index, String name) {
		super(editable);
		this.index = index;
		this.name = name;
	}

	@Override
	public void loadFrom(Attribute source, String language) {
		super.loadFrom(source, language);
		name = source.getName();
	}

	@Override
	public void saveTo(Attribute dest, String language) {
		super.saveTo(dest, language);
		dest.setName(name);
	}

	@Override
	protected void reset() {
		super.reset();
		name = null;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = SurveyObjects.adjustInternalName(name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + index;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttributeFormObject other = (AttributeFormObject) obj;
		if (index != other.index)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}