package org.openforis.collect.metamodel.view;

import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.designer.metamodel.NodeType;

public class AttributeDefView extends NodeDefView {

	private AttributeType attributeType;
	private List<String> fieldNames;
	private List<String> fieldLabels;
	private boolean showInRecordSummaryList;
	private boolean qualifier;
	private Map<String, Boolean> visibilityByField;
	
	public AttributeDefView(int id, String name, String label, AttributeType type, 
			List<String> fieldNames, boolean key, boolean multiple) {
		super(id, name, label, NodeType.ATTRIBUTE, key, multiple);
		this.attributeType = type;
		this.fieldNames = fieldNames;
	}
	
	public AttributeType getAttributeType() {
		return attributeType;
	}
	
	public List<String> getFieldNames() {
		return this.fieldNames;
	}
	
	public boolean isShowInRecordSummaryList() {
		return showInRecordSummaryList;
	}
	
	public void setShowInRecordSummaryList(boolean showInRecordSummaryList) {
		this.showInRecordSummaryList = showInRecordSummaryList;
	}
	
	public boolean isQualifier() {
		return qualifier;
	}
	
	public void setQualifier(boolean qualifier) {
		this.qualifier = qualifier;
	}
	
	public List<String> getFieldLabels() {
		return fieldLabels;
	}
	
	public void setFieldLabels(List<String> fieldLabels) {
		this.fieldLabels = fieldLabels;
	}

	public Map<String, Boolean> getVisibilityByField() {
		return visibilityByField;
	}
	
	public void setVisibilityByField(Map<String, Boolean> visibilityByField) {
		this.visibilityByField = visibilityByField;
	}
}