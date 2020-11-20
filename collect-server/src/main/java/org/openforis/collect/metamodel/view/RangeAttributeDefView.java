package org.openforis.collect.metamodel.view;

import java.util.List;

import org.openforis.collect.designer.metamodel.AttributeType;

public class RangeAttributeDefView extends NumericAttributeDefView {

	public RangeAttributeDefView(int id, String name, String label, AttributeType type, List<String> fieldNames,
			boolean key, boolean multiple) {
		super(id, name, label, type, fieldNames, key, multiple);
	}

}
