package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.RANGE;

import org.openforis.idm.metamodel.RangeAttributeDefinition;

/**
 * 
 * @author G. Miceli
 *
 */
class RangeAttributeXS extends NumericAttributeXS<RangeAttributeDefinition> {

	RangeAttributeXS() {
		super(RANGE);
	}

}
