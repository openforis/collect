package org.openforis.collect.metamodel.view;

import java.util.List;

import org.openforis.collect.designer.metamodel.AttributeType;

public class NumberAttributeDefView extends NumericAttributeDefView {

	public NumberAttributeDefView(int id, String name, String label, AttributeType type, List<String> fieldNames,
			boolean key, boolean multiple, boolean showInRecordSummaryList, boolean qualifier) {
		super(id, name, label, type, fieldNames, key, multiple, showInRecordSummaryList, qualifier);
	}

}
