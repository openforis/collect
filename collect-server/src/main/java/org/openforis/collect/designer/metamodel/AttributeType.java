package org.openforis.collect.designer.metamodel;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public enum AttributeType {
	BOOLEAN, CODE, COORDINATE, DATE, FILE, NUMBER, RANGE, TAXON, TEXT, TIME;
	
	public static AttributeType valueOf(AttributeDefinition defn) {
		return valueOf(defn.getClass());
	}

	public static AttributeType valueOf(Class<? extends AttributeDefinition> type) {
		if ( BooleanAttributeDefinition.class.isAssignableFrom(type) ) {
			return BOOLEAN;
		} else if ( CodeAttributeDefinition.class.isAssignableFrom(type) ) {
			return CODE;
		} else if ( CoordinateAttributeDefinition.class.isAssignableFrom(type) ) {
			return COORDINATE;
		} else if ( DateAttributeDefinition.class.isAssignableFrom(type) ) {
			return DATE;
		} else if ( FileAttributeDefinition.class.isAssignableFrom(type) ) {
			return FILE;
		} else if ( NumberAttributeDefinition.class.isAssignableFrom(type) ) {
			return NUMBER;
		} else if ( RangeAttributeDefinition.class.isAssignableFrom(type) ) {
			return RANGE;
		} else if ( TaxonAttributeDefinition.class.isAssignableFrom(type) ) {
			return TAXON;
		} else if ( TextAttributeDefinition.class.isAssignableFrom(type) ) {
			return TEXT;
		} else if ( TimeAttributeDefinition.class.isAssignableFrom(type) ) {
			return TIME;
		} else {
			throw new IllegalArgumentException("Standard not supported for " + type.getClass().getSimpleName());
		}
	}
	
	public static AttributeType fromLabel(String label) {
		for (AttributeType attributeType : values()) {
			if (attributeType.getLabel().equals(label)) {
				return attributeType;
			}
		}
		return null;
	}
	
	public String getLabel() {
		String labelKey = null;
		switch (this) {
		case BOOLEAN:
			labelKey = "survey.schema.attribute.type.bool";
			break;
		case CODE:
			labelKey = "survey.schema.attribute.type.code";
			break;
		case COORDINATE:
			labelKey = "survey.schema.attribute.type.coordinate";
			break;
		case DATE:
			labelKey = "survey.schema.attribute.type.date";
			break;
		case FILE:
			labelKey = "survey.schema.attribute.type.file";
			break;
		case NUMBER:
			labelKey = "survey.schema.attribute.type.number";
			break;
		case RANGE:
			labelKey = "survey.schema.attribute.type.range";
			break;
		case TAXON:
			labelKey = "survey.schema.attribute.type.taxon";
			break;
		case TEXT:
			labelKey = "survey.schema.attribute.type.text";
			break;
		case TIME:
			labelKey = "survey.schema.attribute.type.time";
			break;
		default:
			break;
		}
		return Labels.getLabel(labelKey);
	}
}