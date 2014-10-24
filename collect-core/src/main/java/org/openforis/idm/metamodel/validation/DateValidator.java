package org.openforis.idm.metamodel.validation;

import java.util.Calendar;

import org.openforis.idm.model.Date;
import org.openforis.idm.model.DateAttribute;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class DateValidator implements ValidationRule<DateAttribute> {

	@Override
	public ValidationResultFlag evaluate(DateAttribute attribute) {
		try {
			Date date = attribute.getValue();
			Calendar cal = date.toCalendar();
			if (cal == null) {
				return ValidationResultFlag.ERROR;
			}
			cal.getTime();
			return ValidationResultFlag.OK;
		} catch (IllegalArgumentException e) {
			return ValidationResultFlag.ERROR;
		}
	}

}
