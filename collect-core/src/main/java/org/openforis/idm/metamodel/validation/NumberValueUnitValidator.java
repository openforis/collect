package org.openforis.idm.metamodel.validation;

import java.util.List;

import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.model.NumberAttribute;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class NumberValueUnitValidator implements ValidationRule<NumberAttribute<?, ?>> {

	@Override
	public ValidationResultFlag evaluate(NumberAttribute<?, ?> attribute) {
		Unit unit = attribute.getUnit();
		NumberAttributeDefinition defn = attribute.getDefinition();
		List<Unit> units = defn.getUnits();
		if ( units.size() > 1  && unit == null ) {
			Number number = attribute.getNumber();
			if ( number != null && number.doubleValue() != 0) {
				return ValidationResultFlag.ERROR;
			} else {
				return ValidationResultFlag.OK;
			}
		} else {
			return ValidationResultFlag.OK;
		}
	}

}
