/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openforis.idm.model.NumericRange;

/**
 * @author M. Togna
 * 
 */
public abstract class AbstractNumericRange<T extends Number> extends AbstractValue implements NumericRange<T> {

	public AbstractNumericRange(String stringValue) {
		super("");
		Matcher matcher = getPattern().matcher(stringValue);
		if (matcher.matches()) {
			String text1 = matcher.group(1);
			String text2 = matcher.group(2);
			setText1(text1);
			setText2(text2);
		} else {
			setText1(stringValue);
		}
		// super(stringValue);

	}

	@Override
	public boolean isFormatValid() {
		if (!this.isBlank()) {
			Matcher matcher = getPattern().matcher(this.getText1());
			return matcher.matches();
		}
		return false;
	}
	
	protected abstract Pattern getPattern();

}
