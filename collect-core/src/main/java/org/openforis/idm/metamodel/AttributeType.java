package org.openforis.idm.metamodel;

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
}