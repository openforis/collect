package org.openforis.collect.metamodel.view;

import java.util.List;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;

public abstract class NumericAttributeDefView extends AttributeDefView {

	private Type numericType;
	private List<PrecisionView> precisions;

	public NumericAttributeDefView(int id, String name, String label, AttributeType type, List<String> fieldNames,
			boolean key, boolean multiple, boolean showInRecordSummaryList, boolean qualifier) {
		super(id, name, label, type, fieldNames, key, multiple, showInRecordSummaryList, qualifier);
	}

	public Type getNumericType() {
		return numericType;
	}

	public void setNumericType(Type numericType) {
		this.numericType = numericType;
	}

	public List<PrecisionView> getPrecisions() {
		return precisions;
	}

	public void setPrecisions(List<PrecisionView> precisions) {
		this.precisions = precisions;
	}

}
