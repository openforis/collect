package org.openforis.collect.designer.form;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.validation.ComparisonCheck;

/**
 * 
 * @author S. Ricci
 *
 */
public class ComparisonCheckFormObject extends CheckFormObject<ComparisonCheck> {
	
	private enum LessThanType {
		LT, LE
	};
	
	private enum GreaterThanType {
		GT, GE
	};
	
	private String greaterThan;
	private String lessThan;
	private GreaterThanType greaterThanType;
	private LessThanType lessThanType;
	
	@Override
	public void saveTo(ComparisonCheck dest, String languageCode) {
		super.saveTo(dest, languageCode);
		resetExpressions(dest);
		String trimmedGreaterThanExpr = StringUtils.trimToNull(greaterThan);
		String trimmedLessThanExpr = StringUtils.trimToNull(lessThan);
		
		if ( trimmedGreaterThanExpr != null && trimmedLessThanExpr != null && 
				greaterThanType == GreaterThanType.GE && lessThanType == LessThanType.LE && 
				trimmedGreaterThanExpr.equals(trimmedLessThanExpr)) {
			dest.setEqualsExpression(trimmedGreaterThanExpr);
		} else {
			if ( trimmedGreaterThanExpr != null ) {
				if ( greaterThanType == GreaterThanType.GE ) {
					dest.setGreaterThanOrEqualsExpression(trimmedGreaterThanExpr);
				} else {
					dest.setGreaterThanExpression(trimmedGreaterThanExpr);
				}
			}
			if ( trimmedLessThanExpr != null ) {
				if ( lessThanType == LessThanType.LE ) {
					dest.setLessThanOrEqualsExpression(trimmedLessThanExpr);
				} else {
					dest.setLessThanExpression(trimmedLessThanExpr);
				}
			}
		}
	}
	
	@Override
	public void loadFrom(ComparisonCheck source, String languageCode) {
		super.loadFrom(source, languageCode);
		if ( StringUtils.isNotBlank(source.getEqualsExpression()) ) {
			greaterThan = lessThan = source.getEqualsExpression();
			greaterThanType = GreaterThanType.GE;
			lessThanType = LessThanType.LE;
		} else {
			if ( StringUtils.isNotBlank(source.getGreaterThanExpression()) ) {
				greaterThan = source.getGreaterThanExpression();
				greaterThanType = GreaterThanType.GT;
			} else if ( StringUtils.isNotBlank(source.getGreaterThanOrEqualsExpression()) ) {
				greaterThan = source.getGreaterThanOrEqualsExpression();
				greaterThanType = GreaterThanType.GE;
			}
			if ( StringUtils.isNotBlank(source.getLessThanExpression()) ) {
				lessThan = source.getLessThanExpression();
				lessThanType = LessThanType.LT;
			} else if ( StringUtils.isNotBlank(source.getLessThanOrEqualsExpression()) ) {
				lessThan = source.getLessThanOrEqualsExpression();
				lessThanType = LessThanType.LE;
			}
		}
	}

	@Override
	protected void reset() {
		super.reset();
		greaterThan = lessThan = null;
		greaterThanType = GreaterThanType.GT;
		lessThanType = LessThanType.LT;
	}
	
	protected void resetExpressions(ComparisonCheck dest) {
		dest.setEqualsExpression(null);
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

	public String getGreaterThanTypeCode() {
		return greaterThanType.name();
	}
	
	public void setGreaterThanTypeCode(String value) {
		this.greaterThanType = GreaterThanType.valueOf(value);
	}
	
	public String getLessThan() {
		return lessThan;
	}

	public void setLessThan(String lessThan) {
		this.lessThan = lessThan;
	}

	public String getLessThanTypeCode() {
		return lessThanType.name();
	}
	
	public void setLessThanTypeCode(String value) {
		this.lessThanType = LessThanType.valueOf(value);
	}
	
}
