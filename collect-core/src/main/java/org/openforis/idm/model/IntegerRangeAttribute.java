package org.openforis.idm.model;

import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Unit;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class IntegerRangeAttribute extends NumericRangeAttribute<IntegerRange, Integer> {

	private static final long serialVersionUID = 1L;
	private static final String TEXT_VALUE_FORMAT = "%d - %d";

	public IntegerRangeAttribute(RangeAttributeDefinition definition) {
		super(definition);
		if (!definition.isInteger()) {
			throw new IllegalArgumentException("RangeAttributeDefinition with type integer required");
		}
	}

	@Override
	protected IntegerRange createRange(Integer from, Integer to, Unit unit) {
		return new IntegerRange(from, to, unit);
	}
	
	@Override
	public String extractTextValue() {
		return String.format(TEXT_VALUE_FORMAT, this.getFrom(), this.getTo());
	}

}
