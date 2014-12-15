/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.openforis.idm.model.RealRange;
import org.openforis.idm.model.RealRangeAttribute;

/**
 * @author M. Togna
 * 
 */
public class RealRangeValidator implements ValidationRule<RealRangeAttribute> {

	@Override
	public ValidationResultFlag evaluate(RealRangeAttribute node) {
		RealRange range = node.getValue();
		if ( range != null ) {
			Double from = range.getFrom();
			Double to = range.getTo();
			
			if ( !( from == null || to == null ) ) {
				boolean valid = to >= from;
				return ValidationResultFlag.valueOf(valid);
			}
		}
		return ValidationResultFlag.OK;
	}

}
