package org.openforis.collect.designer.model;

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

/**
 * 
 * @author S. Ricci
 *
 */
public enum AttributeType {
	BOOLEAN, CODE, COORDINATE, DATE, FILE, NUMBER, RANGE, TAXON, TEXT, TIME;
	
	public static AttributeType typeOf(AttributeDefinition defn) {
		if ( defn instanceof BooleanAttributeDefinition ) {
			return BOOLEAN;
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
			throw new IllegalArgumentException("Type not supported for " + defn.getClass().getSimpleName());
		}
	}
}