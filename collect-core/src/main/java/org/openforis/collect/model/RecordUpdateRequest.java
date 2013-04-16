package org.openforis.collect.model;

import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;


/**
 * 
 * @author S. Ricci
 *
 */
public abstract class RecordUpdateRequest {
	
	public static class AddEntityRequest extends RecordUpdateRequest {
		
		private Integer parentEntityId;
		private String nodeName;
		
		public Integer getParentEntityId() {
			return parentEntityId;
		}

		public void setParentEntityId(Integer parentEntityId) {
			this.parentEntityId = parentEntityId;
		}

		public String getNodeName() {
			return nodeName;
		}

		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}

	}
	
	public static abstract class BaseAttributeRequest<V extends Value> extends RecordUpdateRequest {
		
		private V value;
		private String remarks;
		private FieldSymbol symbol;
		
		public String getRemarks() {
			return remarks;
		}

		public void setRemarks(String remarks) {
			this.remarks = remarks;
		}

		public FieldSymbol getSymbol() {
			return symbol;
		}

		public void setSymbol(FieldSymbol symbol) {
			this.symbol = symbol;
		}

		public V getValue() {
			return value;
		}

		public void setValue(V value) {
			this.value = value;
		}
		
	}
	
	public static class AddAttributeRequest<V extends Value> extends BaseAttributeRequest<V> {

		private Integer parentEntityId;
		private String nodeName;
		
		public Integer getParentEntityId() {
			return parentEntityId;
		}

		public void setParentEntityId(Integer parentEntityId) {
			this.parentEntityId = parentEntityId;
		}

		public String getNodeName() {
			return nodeName;
		}

		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}

	}
	
	public static class UpdateAttributeRequest<V extends Value>  extends BaseAttributeRequest<V> {
		
		private Attribute<?, V> attribute;
		
		public Attribute<?, V> getAttribute() {
			return attribute;
		}
		
		public void setAttribute(Attribute<?, V> attribute) {
			this.attribute = attribute;
		}

	}
	
	public static class UpdateFieldRequest extends RecordUpdateRequest {
		
		private Attribute<?, ?> attribute;
		private Integer fieldIndex;
		private Object value;
		private String remarks;
		private FieldSymbol symbol;
		
		public Attribute<?, ?> getAttribute() {
			return attribute;
		}
		
		public void setAttribute(Attribute<?, ?> attribute) {
			this.attribute = attribute;
		}
		
		public String getRemarks() {
			return remarks;
		}

		public void setRemarks(String remarks) {
			this.remarks = remarks;
		}

		public FieldSymbol getSymbol() {
			return symbol;
		}

		public void setSymbol(FieldSymbol symbol) {
			this.symbol = symbol;
		}

		public Integer getFieldIndex() {
			return fieldIndex;
		}

		public void setFieldIndex(Integer fieldIndex) {
			this.fieldIndex = fieldIndex;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

	}
	
	public static class DeleteNodeRequest extends RecordUpdateRequest {
		
		private Node<?> node;

		public Node<?> getNode() {
			return node;
		}

		public void setNode(Node<?> node) {
			this.node = node;
		}

	}
	
	public static class ApproveMissingValueRequest extends RecordUpdateRequest {
		
		private Integer parentEntityId;
		private String nodeName;
		
		public Integer getParentEntityId() {
			return parentEntityId;
		}

		public void setParentEntityId(Integer parentEntityId) {
			this.parentEntityId = parentEntityId;
		}

		public String getNodeName() {
			return nodeName;
		}

		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}

	}
	
	public static class UpdateRemarksRequest extends RecordUpdateRequest {
		
		private Attribute<?, ?> attribute;
		private Integer fieldIndex;
		private String remarks;
		
		public Attribute<?, ?> getAttribute() {
			return attribute;
		}

		public void setAttribute(Attribute<?, ?> attribute) {
			this.attribute = attribute;
		}
		
		public Integer getFieldIndex() {
			return fieldIndex;
		}
		
		public void setFieldIndex(Integer fieldIndex) {
			this.fieldIndex = fieldIndex;
		}
		
		public String getRemarks() {
			return remarks;
		}
		
		public void setRemarks(String remarks) {
			this.remarks = remarks;
		}
		
	}
	
	public static class ConfirmErrorRequest extends RecordUpdateRequest {
		
		private Attribute<?, ?> attribute;

		public Attribute<?, ?> getAttribute() {
			return attribute;
		}

		public void setAttribute(Attribute<?, ?> attribute) {
			this.attribute = attribute;
		}

	}
	
	public static class ApplyDefaultValueRequest extends RecordUpdateRequest {
		
		private Attribute<?, ?> attribute;

		public Attribute<?, ?> getAttribute() {
			return attribute;
		}

		public void setAttribute(Attribute<?, ?> attribute) {
			this.attribute = attribute;
		}
		
	}
	
}
