package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.NUMBER;

import java.io.IOException;

import org.openforis.idm.metamodel.NumberAttributeDefinition;

/**
 * 
 * @author G. Miceli
 *
 */
class NumberAttributeXS extends NumericAttributeXS<NumberAttributeDefinition> {

	NumberAttributeXS() {
		super(NUMBER);
	}

	@Override
	protected void attributes(NumberAttributeDefinition defn) throws IOException {
		super.attributes(defn);
	}
}
