package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

import java.io.IOException;

import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Precision;

/**
 * 
 * @author G. Miceli
 *
 */
abstract class NumericAttributeXS<T extends NumericAttributeDefinition> extends AttributeDefinitionXS<T> {

	protected NumericAttributeXS(String tag) {
		super(tag);
		addChildMarshallers(new PrecisionXS());
	}

	@Override
	protected void attributes(T defn) throws IOException {
		super.attributes(defn);
		attribute(TYPE, defn.getType().name().toLowerCase());
	}
	
	private class PrecisionXS extends XmlSerializerSupport<Precision, NumericAttributeDefinition> {
		public PrecisionXS() {
			super(PRECISION);
		}
		
		@Override
		protected void marshalInstances(NumericAttributeDefinition defn) throws IOException {
			marshal(defn.getPrecisionDefinitions());
		}
		
		@Override
		protected void attributes(Precision p) throws IOException {
			attribute(DECIMAL_DIGITS, p.getDecimalDigits());
			attribute(UNIT, p.getUnitName());
			if ( p.isDefaultPrecision() ) {
				attribute(DEFAULT, true);
			}
		}
	}
}
