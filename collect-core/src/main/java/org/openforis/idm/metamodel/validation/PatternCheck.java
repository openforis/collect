/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.TextValue;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class PatternCheck extends Check<Attribute<?,?>> {

	private static final long serialVersionUID = 1L;

	private Pattern pattern;
	private String regularExpression;

	public String getRegularExpression() {
		return this.regularExpression;
	}

	public void setRegularExpression(String regularExpression) {
		this.regularExpression = regularExpression;
		initPattern();
	}
	
	private Pattern getPattern() {
		if (pattern == null) {
			initPattern();
		}
		return pattern;
	}

	private void initPattern() {
		this.pattern = Pattern.compile(regularExpression);
	}

	@Override
	public ValidationResultFlag evaluate(Attribute<?,?> node) {
		Object value = node.getValue();
		String string = null;
		if (value instanceof TextValue) {
			string = ((TextValue) value).getValue();
		} else if (value instanceof Code) {
			string = ((Code) value).getCode();
		} else {
			throw new IllegalArgumentException("Pattern check cannot be applied to value type " + value.getClass().getName());
		}
		Matcher matcher = getPattern().matcher(string);
		boolean matches = matcher.matches();
		return ValidationResultFlag.valueOf(matches, this.getFlag());
	}

}
