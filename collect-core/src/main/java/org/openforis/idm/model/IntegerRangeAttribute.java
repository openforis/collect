package org.openforis.idm.model;

import org.openforis.idm.metamodel.RangeAttributeDefinition;

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
	protected IntegerRange createRange(Integer from, Integer to, Integer unitId) {
		return new IntegerRange(from, to, unitId);
	}

}
