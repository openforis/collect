package org.openforis.collect.designer.model;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition;
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
	BOOLEAN, CALCULATED, CODE, COORDINATE, DATE, FILE, NUMBER, RANGE, TAXON, TEXT, TIME;
	
	public static AttributeType valueOf(AttributeDefinition defn) {
		if ( defn instanceof BooleanAttributeDefinition ) {
			return BOOLEAN;
		} else if ( defn instanceof CalculatedAttributeDefinition ) {
			return CALCULATED;
		} else if ( defn instanceof CodeAttributeDefinition ) {
			return CODE;
		} else if ( defn instanceof CoordinateAttributeDefinition ) {
			return COORDINATE;
		} else if ( defn instanceof DateAttributeDefinition ) {
			return DATE;
		} else if ( defn instanceof FileAttributeDefinition ) {
			return FILE;
		} else if ( defn instanceof NumberAttributeDefinition ) {
			return NUMBER;
		} else if ( defn instanceof RangeAttributeDefinition ) {
			return RANGE;
		} else if ( defn instanceof TaxonAttributeDefinition ) {
			return TAXON;
		} else if ( defn instanceof TextAttributeDefinition ) {
			return TEXT;
		} else if ( defn instanceof TimeAttributeDefinition ) {
			return TIME;
		} else {
			throw new IllegalArgumentException("Standard not supported for " + defn.getClass().getSimpleName());
		}
	}

	public String getLabel() {
		String labelKey = null;
		switch (this) {
		case BOOLEAN:
			labelKey = "survey.schema.attribute.type.bool";
			break;
		case CALCULATED:
			labelKey = "survey.schema.attribute.type.calculated";
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