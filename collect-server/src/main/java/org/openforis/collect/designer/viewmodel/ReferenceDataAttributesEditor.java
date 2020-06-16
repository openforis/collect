package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.form.AttributeFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition.Attribute;
import org.openforis.idm.metamodel.Survey;
import org.zkoss.zul.ListModelList;

class ReferenceDataAttributesEditor {

	private static final String ERROR_ATTRIBUTE_NAME_REQUIRED = "global.validation.name_required";
	private static final String ERROR_ATTRIBUTE_NAME_INVALID = "global.validation.internal_name.invalid_value";
	private static final String ERROR_ATTRIBUTE_NAME_DUPLICATE = "global.item.validation.name_already_defined";

	private ReferenceDataDefinition referenceDataDefinition;
	private List<String> fixedColumnNames;
	private ListModelList<AttributeFormObject> attributes;

	public ReferenceDataAttributesEditor(List<String> fixedColumnNames,
			ReferenceDataDefinition referenceDataDefinition) {
		this.fixedColumnNames = fixedColumnNames;
		this.referenceDataDefinition = referenceDataDefinition;
	}

	public List<AttributeFormObject> getAttributes() {
		if (attributes == null) {
			attributes = new ListModelList<AttributeFormObject>();
			for (String colName : fixedColumnNames) {
				attributes.add(new AttributeFormObject(false, attributes.size(), colName));
			}
			List<String> infoAttributeNames = referenceDataDefinition.getAttributeNames();
			for (String infoAttributeName : infoAttributeNames) {
				attributes.add(new AttributeFormObject(true, attributes.size(), infoAttributeName));
			}
		}
		return attributes;
	}

	public void changeAttributeEditableStatus(AttributeFormObject attribute) {
		attribute.setEditingStatus(!attribute.getEditingStatus());
		refreshAttributeColumnTemplate(attribute);
	}

	private void refreshAttributeColumnTemplate(AttributeFormObject attribute) {
		// replace the element in the collection by itself to trigger a model update
		int index = attributes.indexOf(attribute);
		attributes.set(index, attribute);
	}

	public boolean confirmAttributeUpdate(AttributeFormObject attribute) {
		int infoAttributeIndex = attribute.getIndex() - fixedColumnNames.size();
		Attribute oldAttribute = referenceDataDefinition.getAttributes().get(infoAttributeIndex);
		if (oldAttribute.getName().equals(attribute.getName())) {
			// no change
			changeAttributeEditableStatus(attribute);
			return false;
		} else if (validateAttribute(attribute)) {
			changeAttributeEditableStatus(attribute);
			referenceDataDefinition.setAttribute(infoAttributeIndex, new Attribute(attribute.getName()));
			return true;
		} else {
			return false;
		}
	}

	private boolean validateAttribute(AttributeFormObject attribute) {
		String name = attribute.getName();
		String error = null;
		if (StringUtils.isBlank(name)) {
			error = ERROR_ATTRIBUTE_NAME_REQUIRED;
		} else if (!name.matches(Survey.INTERNAL_NAME_REGEX)) {
			error = ERROR_ATTRIBUTE_NAME_INVALID;
		} else {
			// validate name uniqueness
			for (int index = 0; index < attributes.size(); index++) {
				AttributeFormObject attributeFormObject = attributes.get(index);
				if (name.equals(attributeFormObject.getName()) && attribute.getIndex() != index) {
					error = ERROR_ATTRIBUTE_NAME_DUPLICATE;
					break;
				}
			}
		}
		if (error == null) {
			return true;
		} else {
			MessageUtil.showError(error);
			return false;
		}
	}

}