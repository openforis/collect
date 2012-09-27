/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.Check.Flag;
import org.openforis.idm.metamodel.validation.ComparisonCheck;

/**
 * @author S. Ricci
 *
 */
public class NumericAttributeDefinitionFormObject<T extends NumericAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	private String type;
	private List<Precision> precisions;
	
	private String comparisonCheckFlag;
	private String greaterThan;
	private boolean greaterThanEqual;
	private String lessThan;
	private boolean lessThanEqual;
	
	public NumericAttributeDefinitionFormObject() {
		type = NumericAttributeDefinition.Type.INTEGER.name();
		comparisonCheckFlag = Check.Flag.ERROR.name();
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		Type typeEnum = null;
		if ( type != null ) {
			typeEnum = NumericAttributeDefinition.Type.valueOf(type);
		}
		dest.setType(typeEnum);
		dest.removeAllPrecisionDefinitions();
		if ( precisions != null ) {
			for (Precision precision : precisions) {
				dest.addPrecisionDefinition(precision);
			}
		}
		saveChecks(dest);
	}

	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		type = source.getType() != null ? source.getType().name(): null;
		precisions = new ArrayList<Precision>(source.getPrecisionDefinitions());
		loadChecks(source);
	}

	protected void saveChecks(T dest) {
		dest.removeAllChecks();
		ComparisonCheck comparisonCheck = createComparisonCheck();
		dest.addCheck(comparisonCheck);
	}

	protected ComparisonCheck createComparisonCheck() {
		ComparisonCheck comparisonCheck = new ComparisonCheck();
		Flag flag = StringUtils.isNotBlank(comparisonCheckFlag) ? Flag.valueOf(comparisonCheckFlag): null;
		comparisonCheck.setFlag(flag);
		boolean empty = true;
		if ( greaterThan != null && greaterThan.equals(lessThan) && greaterThanEqual && lessThanEqual ) {
			comparisonCheck.setEqualsExpression(greaterThan);
			empty = false;
		}
		if ( StringUtils.isNotBlank(greaterThan) ) {
			if ( greaterThanEqual ) {
				comparisonCheck.setGreaterThanOrEqualsExpression(greaterThan);
			} else {
				comparisonCheck.setGreaterThanExpression(greaterThan);
			}
			empty = false;
		}
		if ( StringUtils.isNotBlank(lessThan) ) {
			if ( lessThanEqual ) {
				comparisonCheck.setLessThanOrEqualsExpression(lessThan);
			} else {
				comparisonCheck.setLessThanExpression(lessThan);
			}
			empty = false;
		}
		return empty ? null: comparisonCheck;
	}

	protected void loadChecks(T source) {
		resetCheckInfo();
		List<Check<?>> checks = source.getChecks();
		for (Check<?> check : checks) {
			if ( check instanceof ComparisonCheck ) {
				loadCheck((ComparisonCheck) check);
			}
		}
	}

	protected void resetCheckInfo() {
		comparisonCheckFlag = Check.Flag.ERROR.name();
		greaterThan = lessThan = null;
		greaterThanEqual = lessThanEqual = false;
	}

	protected void loadCheck(ComparisonCheck check) {
		comparisonCheckFlag = check.getFlag().name();
		if ( StringUtils.isNotBlank(check.getEqualsExpression()) ) {
			greaterThan = lessThan = check.getEqualsExpression();
			greaterThanEqual = lessThanEqual = true;
		} else {
			if ( StringUtils.isNotBlank(check.getGreaterThanExpression()) ) {
				greaterThan = check.getGreaterThanExpression();
				greaterThanEqual = false;
			} else if ( StringUtils.isNotBlank(check.getGreaterThanOrEqualsExpression()) ) {
				greaterThan = check.getGreaterThanOrEqualsExpression();
				greaterThanEqual = true;
			}
			if ( StringUtils.isNotBlank(check.getLessThanExpression()) ) {
				lessThan = check.getLessThanExpression();
				lessThanEqual = false;
			} else if ( StringUtils.isNotBlank(check.getLessThanOrEqualsExpression()) ) {
				lessThan = check.getLessThanOrEqualsExpression();
				lessThanEqual = true;
			}
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Precision> getPrecisions() {
		return precisions;
	}

	public void setPrecisions(List<Precision> precisions) {
		this.precisions = precisions;
	}

	public boolean isGreaterThanEqual() {
		return greaterThanEqual;
	}

	public void setGreaterThanEqual(boolean greaterThanEqual) {
		this.greaterThanEqual = greaterThanEqual;
	}

	public boolean isLessThanEqual() {
		return lessThanEqual;
	}

	public void setLessThanEqual(boolean lessThanEqual) {
		this.lessThanEqual = lessThanEqual;
	}

	public String getGreaterThan() {
		return greaterThan;
	}

	public void setGreaterThan(String greaterThan) {
		this.greaterThan = greaterThan;
	}

	public String getLessThan() {
		return lessThan;
	}

	public void setLessThan(String lessThan) {
		this.lessThan = lessThan;
	}

	public String getComparisonCheckFlag() {
		return comparisonCheckFlag;
	}

	public void setComparisonCheckFlag(String comparisonCheckFlag) {
		this.comparisonCheckFlag = comparisonCheckFlag;
	}

}
