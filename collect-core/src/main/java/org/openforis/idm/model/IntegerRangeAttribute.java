package org.openforis.idm.model;

import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Unit;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class IntegerRangeAttribute extends NumericRangeAttribute<IntegerRange, Integer> {

	private static final long serialVersionUID = 1L;

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

}
