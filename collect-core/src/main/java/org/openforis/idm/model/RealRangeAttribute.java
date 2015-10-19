package org.openforis.idm.model;

import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Unit;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class RealRangeAttribute extends NumericRangeAttribute<RealRange, Double> {

	private static final long serialVersionUID = 1L;
	private static final String TEXT_VALUE_FORMAT = "%f - %f";

	public RealRangeAttribute(RangeAttributeDefinition definition) {
		super(definition);
		if (!definition.isReal()) {
			throw new IllegalArgumentException("Attempted to create RealRangeAttribute with integer definition");
		}
	}

	@Override
	protected RealRange createRange(Double from, Double to, Unit unit) {
		return new RealRange(from, to, unit);
	}
	
	@Override
	public String extractTextValue() {
		return String.format(TEXT_VALUE_FORMAT, this.getFrom(), this.getTo());
	}
}
