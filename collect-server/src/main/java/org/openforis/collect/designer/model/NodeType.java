package org.openforis.collect.designer.model;

import java.util.Locale;

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
			Object[] args = new String[]{nodeTypeLabel.toLowerCase(Locale.ENGLISH)};
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
		return createNodeDefinition(survey, nodeType, attrType, null);
	}
	
	public static NodeDefinition createNodeDefinition(Survey survey, NodeType nodeType, AttributeType attrType, Integer id) {
		NodeDefinition result;
		Schema schema = survey.getSchema();
		switch(nodeType) {
		case ENTITY:
			if (id == null) {
				result = schema.createEntityDefinition();
			} else {
				result = schema.createEntityDefinition(id);
			}
			break;
		case ATTRIBUTE:
			switch(attrType) {
			case BOOLEAN:
				if (id == null) {
					result = schema.createBooleanAttributeDefinition();
				} else {
					result = schema.createBooleanAttributeDefinition(id);
				}
				break;
			case CODE:
				if (id == null) {
					result = schema.createCodeAttributeDefinition();
				} else {
					result = schema.createCodeAttributeDefinition(id);
				}
				break;
			case COORDINATE:
				if (id == null) {
					result = schema.createCoordinateAttributeDefinition();
				} else {
					result = schema.createCoordinateAttributeDefinition(id);
				}
				break;
			case DATE:
				if (id == null) {
					result = schema.createDateAttributeDefinition();
				} else {
					result = schema.createDateAttributeDefinition(id);
				}
				break;
			case FILE:
				if (id == null) {
					result = schema.createFileAttributeDefinition();
				} else {
					result = schema.createFileAttributeDefinition(id);
				}
				break;
			case NUMBER:
				if (id == null) {
					result = schema.createNumberAttributeDefinition();
				} else {
					result = schema.createNumberAttributeDefinition(id);
				}
				break;
			case RANGE:
				if (id == null) {
					result = schema.createRangeAttributeDefinition();
				} else {
					result = schema.createRangeAttributeDefinition(id);
				}
				break;
			case TAXON:
				if (id == null) {
					result = schema.createTaxonAttributeDefinition();
				} else {
					result = schema.createTaxonAttributeDefinition(id);
				}
				break;
			case TEXT:
				if (id == null) {
					result = schema.createTextAttributeDefinition();
				} else {
					result = schema.createTextAttributeDefinition(id);
				}
				break;
			case TIME:
				if (id == null) {
					result = schema.createTimeAttributeDefinition();
				} else {
					result = schema.createTimeAttributeDefinition(id);
				}
				break;
			default:
				throw new IllegalStateException("Attribute type not supported: " + attrType);
			}
			break;
		default:
			throw new IllegalStateException("Node type not supported: " + nodeType);
		}
		
		setDefaults(result);
		
		return result;
	}

	private static void setDefaults(NodeDefinition result) {
		result.setAlwaysRequired();
	}

}
