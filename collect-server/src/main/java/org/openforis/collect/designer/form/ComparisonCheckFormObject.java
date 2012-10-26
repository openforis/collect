package org.openforis.collect.designer.form;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.validation.ComparisonCheck;

/**
 * 
 * @author S. Ricci
 *
 */
public class ComparisonCheckFormObject extends CheckFormObject<ComparisonCheck> {
	
	private String message;
	private String greaterThan;
	private boolean greaterThanEqual;
	private String lessThan;
	private boolean lessThanEqual;
	
	@Override
	public void saveTo(ComparisonCheck dest, String languageCode) {
		super.saveTo(dest, languageCode);
		resetExpressions(dest);
		dest.setMessage(languageCode, message);
		if ( greaterThanEqual ) {
			dest.setGreaterThanOrEqualsExpression(greaterThan);
		} else {
			dest.setGreaterThanExpression(greaterThan);
		}
		if ( lessThanEqual ) {
			dest.setLessThanOrEqualsExpression(lessThan);
		} else {
			dest.setLessThanExpression(lessThan);
		}
	}
	
	@Override
	public void loadFrom(ComparisonCheck source, String languageCode) {
		super.loadFrom(source, languageCode);
		
		if ( StringUtils.isNotBlank(source.getEqualsExpression()) ) {
			greaterThan = lessThan = source.getEqualsExpression();
			greaterThanEqual = lessThanEqual = true;
		} else {
			if ( StringUtils.isNotBlank(source.getGreaterThanExpression()) ) {
				greaterThan = source.getGreaterThanExpression();
				greaterThanEqual = false;
			} else if ( StringUtils.isNotBlank(source.getGreaterThanOrEqualsExpression()) ) {
				greaterThan = source.getGreaterThanOrEqualsExpression();
				greaterThanEqual = true;
			}
			if ( StringUtils.isNotBlank(source.getLessThanExpression()) ) {
				lessThan = source.getLessThanExpression();
				lessThanEqual = false;
			} else if ( StringUtils.isNotBlank(source.getLessThanOrEqualsExpression()) ) {
				lessThan = source.getLessThanOrEqualsExpression();
				lessThanEqual = true;
			}
		}
	}

	protected void reset() {
		super.reset();
		message = null;
		greaterThan = lessThan = null;
		greaterThanEqual = lessThanEqual = false;
	}
	
	protected void resetExpressions(ComparisonCheck dest) {
		dest.setGreaterThanExpression(null);
		dest.setGreaterThanOrEqualsExpression(null);
		dest.setLessThanExpression(null);
		dest.setLessThanOrEqualsExpression(null);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getGreaterThan() {
		return greaterThan;
	}

	public void setGreaterThan(String greaterThan) {
		this.greaterThan = greaterThan;
	}

	public boolean isGreaterThanEqual() {
		return greaterThanEqual;
	}

	public void setGreaterThanEqual(boolean greaterThanEqual) {
		this.greaterThanEqual = greaterThanEqual;
	}

	public String getLessThan() {
		return lessThan;
	}

	public void setLessThan(String lessThan) {
		this.lessThan = lessThan;
	}

	public boolean isLessThanEqual() {
		return lessThanEqual;
	}

	public void setLessThanEqual(boolean lessThanEqual) {
		this.lessThanEqual = lessThanEqual;
	}

}
