package org.openforis.collect.metamodel.view;

import java.util.List;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.metamodel.ui.UIOptions.CoordinateAttributeFieldsOrder;

public class CoordinateAttributeDefView extends AttributeDefView {

	private CoordinateAttributeFieldsOrder fieldsOrder;
	private boolean showSrsField;
	private boolean includeAltitudeField;
	private boolean includeAccuracyField;

	public CoordinateAttributeDefView(int id, String name, String label, AttributeType type, List<String> fieldNames,
			boolean key, boolean multiple) {
		super(id, name, label, type, fieldNames, key, multiple);
	}

	public CoordinateAttributeFieldsOrder getFieldsOrder() {
		return fieldsOrder;
	}

	public void setFieldsOrder(CoordinateAttributeFieldsOrder fieldsOrder) {
		this.fieldsOrder = fieldsOrder;
	}

	public boolean isShowSrsField() {
		return showSrsField;
	}

	public void setShowSrsField(boolean showSrsField) {
		this.showSrsField = showSrsField;
	}

	public boolean isIncludeAltitudeField() {
		return includeAltitudeField;
	}

	public void setIncludeAltitudeField(boolean includeAltitudeField) {
		this.includeAltitudeField = includeAltitudeField;
	}

	public boolean isIncludeAccuracyField() {
		return includeAccuracyField;
	}

	public void setIncludeAccuracyField(boolean includeAccuracyField) {
		this.includeAccuracyField = includeAccuracyField;
	}
}
