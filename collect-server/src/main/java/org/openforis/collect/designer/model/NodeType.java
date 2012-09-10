package org.openforis.collect.designer.model;

import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public enum NodeType {
	ENTITY, ATTRIBUTE;
	
	public static NodeType typeOf(NodeDefinition nodeDefn) {
		if ( nodeDefn instanceof EntityDefinition ) {
			return ENTITY;
		} else {
			return ATTRIBUTE;
		}
	}
	
	public static NodeDefinition createNodeDefinition(Survey survey, String nodeType, String attributeType) {
		NodeDefinition result;
		NodeType nodeTypeEnum = NodeType.valueOf(nodeType);
		switch(nodeTypeEnum) {
		case ENTITY:
			result = new EntityDefinition();
			break;
		case ATTRIBUTE:
			AttributeType attrType = AttributeType.valueOf(attributeType);
			switch(attrType) {
			case BOOLEAN:
				result = new BooleanAttributeDefinition();
				break;
			case CODE:
				result = new CodeAttributeDefinition();
				break;
			case COORDINATE:
				result = new CoordinateAttributeDefinition();
				break;
			case DATE:
				result = new DateAttributeDefinition();
				break;
			case FILE:
				result = new FileAttributeDefinition();
				break;
			case NUMBER:
				result = new NumberAttributeDefinition();
				break;
			case RANGE:
				result = new RangeAttributeDefinition();
				break;
			case TAXON:
				result = new TaxonAttributeDefinition();
				break;
			case TEXT:
				result = new TextAttributeDefinition();
				break;
			case TIME:
				result = new TimeAttributeDefinition();
				break;
			default:
				throw new IllegalStateException("Attribute type not supported: " + attributeType);
			}
			break;
		default:
			throw new IllegalStateException("Node type not supported: " + nodeType);
		}
		result.setSchema(survey.getSchema());
		return result;
	}
	
}
