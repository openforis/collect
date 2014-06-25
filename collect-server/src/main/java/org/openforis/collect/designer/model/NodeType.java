package org.openforis.collect.designer.model;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public enum NodeType {
	ENTITY, ATTRIBUTE;
	
	public String getLabel() {
		String labelKey;
		switch(this) {
		case ATTRIBUTE:
			labelKey = "survey.schema.node.type.attribute";
			break;
		case ENTITY:
			labelKey = "survey.schema.node.type.entity";
			break;
		default:
			throw new IllegalArgumentException("Standard not supported: " + this.name());
		}
		return Labels.getLabel(labelKey);
	}
	
	public static String getHeaderLabel(NodeDefinition nodeDefn, boolean rootEntity, boolean detached) {
		String messageKey;
		String nodeTypeLabel = null;
		NodeType nodeType = NodeType.valueOf(nodeDefn);
		switch (nodeType) {
		case ENTITY:
			if ( rootEntity ) {
				messageKey = "survey.schema.node_detail_title.root_entity";
			} else {
				messageKey = "survey.schema.node_detail_title.entity";
			}
			nodeTypeLabel = Labels.getLabel(messageKey);
			break;
		case ATTRIBUTE:
			messageKey = "survey.schema.node_detail_title.attribute";
			AttributeType attrType = AttributeType.valueOf((AttributeDefinition) nodeDefn);
			Object[] args = new String[]{attrType.getLabel()};
			nodeTypeLabel = Labels.getLabel(messageKey, args);
			break;
		}
		String result;
		if ( detached ) {
			Object[] args = new String[]{nodeTypeLabel.toLowerCase()};
			result = Labels.getLabel("survey.schema.node_detail_title.new_node", args);
		} else {
			result = nodeTypeLabel;
		}
		return result;
	}
	
	public static NodeType valueOf(NodeDefinition nodeDefn) {
		if ( nodeDefn instanceof EntityDefinition ) {
			return ENTITY;
		} else {
			return ATTRIBUTE;
		}
	}
	
	public static NodeDefinition createNodeDefinition(Survey survey, NodeType nodeType, AttributeType attrType) {
		NodeDefinition result;
		Schema schema = survey.getSchema();
		switch(nodeType) {
		case ENTITY:
			result = schema.createEntityDefinition();
			break;
		case ATTRIBUTE:
			switch(attrType) {
			case BOOLEAN:
				result = schema.createBooleanAttributeDefinition();
				break;
			case CALCULATED:
				result = schema.createCalculatedAttributeDefinition();
				break;
			case CODE:
				result = schema.createCodeAttributeDefinition();
				break;
			case COORDINATE:
				result = schema.createCoordinateAttributeDefinition();
				break;
			case DATE:
				result = schema.createDateAttributeDefinition();
				break;
			case FILE:
				result = schema.createFileAttributeDefinition();
				break;
			case NUMBER:
				result = schema.createNumberAttributeDefinition();
				break;
			case RANGE:
				result = schema.createRangeAttributeDefinition();
				break;
			case TAXON:
				result = schema.createTaxonAttributeDefinition();
				break;
			case TEXT:
				result = schema.createTextAttributeDefinition();
				break;
			case TIME:
				result = schema.createTimeAttributeDefinition();
				break;
			default:
				throw new IllegalStateException("Attribute type not supported: " + attrType);
			}
			break;
		default:
			throw new IllegalStateException("Node type not supported: " + nodeType);
		}
		return result;
	}

}
