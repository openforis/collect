package org.openforis.idm.metamodel.validation;

import java.util.List;

import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.model.NumericRangeAttribute;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class NumericRangeUnitValidator implements ValidationRule<NumericRangeAttribute<?, ?>> {

	@Override
	public ValidationResultFlag evaluate(NumericRangeAttribute<?, ?> attribute) {
		Unit unit = attribute.getUnit();
		RangeAttributeDefinition defn = attribute.getDefinition();
		List<Unit> units = defn.getUnits();
		if ( units.size() > 1  && unit == null ) {
			Number from = attribute.getFrom();
			Number to = attribute.getTo();
			if ( from != null && from.doubleValue() != 0 || 
					to != null && to.doubleValue() != 0 ) {
				return ValidationResultFlag.ERROR;
			} else {
				return ValidationResultFlag.OK;
			}
		} else {
			return ValidationResultFlag.OK;
		}
	}

}
