package org.openforis.collect.remoting.service;

import org.openforis.collect.model.FieldSymbol;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;


/**
 * 
 * @author S. Ricci
 *
 */
public abstract class NodeUpdateRequest {
	
	public static class EntityAddRequest extends NodeUpdateRequest {
		
		private Entity parentEntity;
		private String nodeName;
		
		public Entity getParentEntity() {
			return parentEntity;
		}

		public void setParentEntity(Entity parentEntity) {
			this.parentEntity = parentEntity;
		}

		public String getNodeName() {
			return nodeName;
		}
		
		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}
		
	}
	
	public static abstract class BaseAttributeUpdateRequest<V extends Value> extends NodeUpdateRequest {
		
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
	
	public static class AttributeAddRequest<V extends Value> extends BaseAttributeUpdateRequest<V> {

		private Entity parentEntity;
		private String nodeName;
		
		public Entity getParentEntity() {
			return parentEntity;
		}

		public void setParentEntity(Entity parentEntity) {
			this.parentEntity = parentEntity;
		}

		public String getNodeName() {
			return nodeName;
		}
		
		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}
		
	}
	
	public static class AttributeUpdateRequest<V extends Value>  extends BaseAttributeUpdateRequest<V> {
		
		private Attribute<?, V> attribute;
		
		public Attribute<?, V> getAttribute() {
			return attribute;
		}
		
		public void setAttribute(Attribute<?, V> attribute) {
			this.attribute = attribute;
		}

	}
	
	public static class FieldUpdateRequest extends NodeUpdateRequest {
		
		private Field<?> field;
		private Object value;
		private String remarks;
		private FieldSymbol symbol;
		
		public Field<?> getField() {
			return field;
		}
		
		public void setField(Field<?> field) {
			this.field = field;
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

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

	}
	
	public static class NodeDeleteRequest extends NodeUpdateRequest {
		
		private Node<?> node;

		public Node<?> getNode() {
			return node;
		}

		public void setNode(Node<?> node) {
			this.node = node;
		}

	}
	
	public static class MissingValueApproveRequest extends NodeUpdateRequest {
		
		private Entity parentEntity;
		private String nodeName;
		
		public Entity getParentEntity() {
			return parentEntity;
		}

		public void setParentEntity(Entity parentEntity) {
			this.parentEntity = parentEntity;
		}

		public String getNodeName() {
			return nodeName;
		}

		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}

	}
	
	public static class RemarksUpdateRequest extends NodeUpdateRequest {
		
		private Field<?> field;
		private String remarks;
		
		public Field<?> getField() {
			return field;
		}

		public void setField(Field<?> field) {
			this.field = field;
		}
		
		public String getRemarks() {
			return remarks;
		}
		
		public void setRemarks(String remarks) {
			this.remarks = remarks;
		}
	}
	
	public static class ErrorConfirmRequest extends NodeUpdateRequest {
		
		private Attribute<?, ?> attribute;

		public Attribute<?, ?> getAttribute() {
			return attribute;
		}

		public void setAttribute(Attribute<?, ?> attribute) {
			this.attribute = attribute;
		}

	}
	
	public static class DefaultValueApplyRequest extends NodeUpdateRequest {
		
		private Attribute<?, ?> attribute;

		public Attribute<?, ?> getAttribute() {
			return attribute;
		}

		public void setAttribute(Attribute<?, ?> attribute) {
			this.attribute = attribute;
		}
		
	}
	
}
