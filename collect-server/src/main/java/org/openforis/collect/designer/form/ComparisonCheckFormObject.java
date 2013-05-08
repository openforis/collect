package org.openforis.collect.designer.form;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.validation.ComparisonCheck;

/**
 * 
 * @author S. Ricci
 *
 */
public class ComparisonCheckFormObject extends CheckFormObject<ComparisonCheck> {
	
	private String greaterThan;
	private boolean greaterThanEqual;
	private String lessThan;
	private boolean lessThanEqual;
	
	@Override
	public void saveTo(ComparisonCheck dest, String languageCode) {
		super.saveTo(dest, languageCode);
		resetExpressions(dest);
		String trimmedGreaterThanExpr = StringUtils.trimToNull(greaterThan);
		if ( trimmedGreaterThanExpr != null ) {
			if ( greaterThanEqual ) {
				dest.setGreaterThanOrEqualsExpression(trimmedGreaterThanExpr);
			} else {
				dest.setGreaterThanExpression(trimmedGreaterThanExpr);
			}
		}
		String trimmedLessThanExpr = StringUtils.trimToNull(lessThan);
		if ( trimmedLessThanExpr != null ) {
			if ( lessThanEqual ) {
				dest.setLessThanOrEqualsExpression(trimmedLessThanExpr);
			} else {
				dest.setLessThanExpression(trimmedLessThanExpr);
			}
		}
	}
	
	@Override
	public void loadFrom(ComparisonCheck source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
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

	@Override
	protected void reset() {
		super.reset();
		greaterThan = lessThan = null;
		greaterThanEqual = lessThanEqual = false;
	}
	
	protected void resetExpressions(ComparisonCheck dest) {
		dest.setGreaterThanExpression(null);
		dest.setGreaterThanOrEqualsExpression(null);
		dest.setLessThanExpression(null);
		dest.setLessThanOrEqualsExpression(null);
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
