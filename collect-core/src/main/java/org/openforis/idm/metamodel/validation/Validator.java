/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import java.util.List;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.IntegerRangeAttribute;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.NumericRangeAttribute;
import org.openforis.idm.model.RealRangeAttribute;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.model.TimeAttribute;

/**
 * @author M. Togna
 * 
 */
public class Validator {

	public ValidationResults validate(Attribute<?, ?> attribute) {
		ValidationResults results = new ValidationResults();
		
		// skip validations if attribute is calculated
		AttributeDefinition definition = attribute.getDefinition();
		if ( definition.isCalculated() ) {
			return results;
		}
		if ( ! attribute.isEmpty() ) {
			validateAttributeValue(attribute, results);
			if ( !results.hasErrors() ) {
				validateAttributeChecks(attribute, results);
			}
		}
		return results;
	}

	protected void validateEntityKeys(Attribute<?, ?> attribute, ValidationResults results) {
		EntityKeyValidator validator = new EntityKeyValidator();
		ValidationResultFlag flag = validator.evaluate(attribute);
		results.addResult(validator, flag);
	}
	
	public ValidationResultFlag validateMinCount(Entity entity, String childName) {
		return validateMinCount(entity, getChildDefinition(entity, childName));
	}
		
	public ValidationResultFlag validateMinCount(Entity entity, NodeDefinition childDef) {
		MinCountValidator v = getMinCountValidator(childDef);
		ValidationResultFlag result = v.evaluate(entity);
		return result;
	}
	
	public ValidationResultFlag validateMaxCount(Entity entity, String childName) {
		return validateMaxCount(entity, getChildDefinition(entity, childName));
	}
	
	public ValidationResultFlag validateMaxCount(Entity entity, NodeDefinition childDef) {
		MaxCountValidator v = getMaxCountValidator(childDef);
		ValidationResultFlag result = v.evaluate(entity);
		return result;
	}

	protected MinCountValidator getMinCountValidator(NodeDefinition defn) {
		return new MinCountValidator(defn);
	}

	protected MaxCountValidator getMaxCountValidator(NodeDefinition defn) {
		return new MaxCountValidator(defn);
	}
	
	protected TaxonVernacularLanguageValidator getTaxonVernacularLanguageValidator() {
		return new TaxonVernacularLanguageValidator();
	}
	
	protected CodeValidator getCodeValidator() {
		return new CodeValidator();
	}
	
	protected CodeParentValidator getCodeParentValidator() {
		return new CodeParentValidator();
	}
	
	private NodeDefinition getChildDefinition(Entity entity, String childName) {
		EntityDefinition entityDefn = entity.getDefinition();
		NodeDefinition childDefn = entityDefn.getChildDefinition(childName);
		return childDefn;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void validateAttributeChecks(Attribute<?, ?> attribute, ValidationResults results) {
		AttributeDefinition defn = attribute.getDefinition();
		List<Check<?>> checks = defn.getChecks();
		for (Check check : checks) {
			if (check.evaluateCondition(attribute)) {
				ValidationResultFlag result = check.evaluate(attribute);
				results.addResult(check, result);
			}
		}
	}

	protected void validateAttributeValue(Attribute<?, ?> attribute, ValidationResults results) {
		if (attribute instanceof CodeAttribute) {
			validateCodeAttributeValue((CodeAttribute) attribute, results);
		} else if (attribute instanceof CoordinateAttribute) {
			validateCoordinateAttributeValue((CoordinateAttribute) attribute, results);
		} else if (attribute instanceof DateAttribute) {
			validateDateAttributeValue((DateAttribute) attribute, results);
		} else if (attribute instanceof NumberAttribute) {
			validateNumericAttributeValue((NumberAttribute<?, ?>) attribute, results);
		} else if (attribute instanceof IntegerRangeAttribute) {
			validateIntegerRangeAttributeValue((IntegerRangeAttribute) attribute, results);
		} else if (attribute instanceof RealRangeAttribute) {
			validateRealRangeAttributeValue((RealRangeAttribute) attribute, results);
		} else if (attribute instanceof TimeAttribute) {
			validateTimeAttributeValue((TimeAttribute) attribute, results);
		} else if (attribute instanceof TaxonAttribute) {
			validateTaxonAttributeValue((TaxonAttribute) attribute, results);
		}
		AttributeDefinition defn = attribute.getDefinition();
		if ( defn instanceof KeyAttributeDefinition && ((KeyAttributeDefinition) defn).isKey() ) {
			validateEntityKeys(attribute, results);
		}
	}

	private void validateTimeAttributeValue(TimeAttribute timeAttribute, ValidationResults results) {
		TimeValidator validator = new TimeValidator();
		ValidationResultFlag result = validator.evaluate(timeAttribute);
		results.addResult(validator, result);
	}

	private void validateDateAttributeValue(DateAttribute attribute, ValidationResults results) {
		DateValidator validator = new DateValidator();
		ValidationResultFlag result = validator.evaluate(attribute);
		results.addResult(validator, result);
	}

	private void validateCoordinateAttributeValue(CoordinateAttribute attribute, ValidationResults results) {
		CoordinateValidator validator = new CoordinateValidator();
		ValidationResultFlag result = validator.evaluate(attribute);
		results.addResult(validator, result);
	}

	private void validateCodeAttributeValue(CodeAttribute attribute, ValidationResults results) {
		CodeParentValidator parentValidator = getCodeParentValidator();
		ValidationResultFlag validParent = parentValidator.evaluate(attribute);
		if (validParent == ValidationResultFlag.OK ) {
			if (! attribute.isEmpty()) {
				CodeValidator codeValidator = getCodeValidator();
				ValidationResultFlag result = codeValidator.evaluate(attribute);
				results.addResult(codeValidator, result);
			}
		} else {
			results.addResult(parentValidator, ValidationResultFlag.WARNING);
		}
	}
	
	protected void validateNumericAttributeValue(NumberAttribute<?, ?> attribute, ValidationResults results) {
		validateNumericAttributeUnit(attribute, results);
	}

	protected void validateNumericAttributeUnit(NumberAttribute<?, ?> attribute, ValidationResults results) {
		NumberValueUnitValidator validator = new NumberValueUnitValidator();
		ValidationResultFlag result = validator.evaluate(attribute);
		results.addResult(validator, result);
	}
	
	protected void validateIntegerRangeAttributeValue(IntegerRangeAttribute attribute, ValidationResults results) {
		IntegerRangeValidator validator = new IntegerRangeValidator();
		ValidationResultFlag result = validator.evaluate(attribute);
		results.addResult(validator, result);
		
		validateNumericRangeUnit(attribute, results);
	}

	protected void validateRealRangeAttributeValue(RealRangeAttribute attribute, ValidationResults results) {
		RealRangeValidator validator = new RealRangeValidator();
		ValidationResultFlag result = validator.evaluate(attribute);
		results.addResult(validator, result);
		
		validateNumericRangeUnit(attribute, results);
	}

	protected void validateNumericRangeUnit(NumericRangeAttribute<?, ?> attribute,
			ValidationResults results) {
		NumericRangeUnitValidator unitValidator = new NumericRangeUnitValidator();
		ValidationResultFlag unitValidationResult = unitValidator.evaluate(attribute);
		results.addResult(unitValidator, unitValidationResult);
	}
	
	protected void validateTaxonAttributeValue(TaxonAttribute attribute,
			ValidationResults results) {
		TaxonVernacularLanguageValidator vernacularLanguageValidator = getTaxonVernacularLanguageValidator();
		ValidationResultFlag validationResultFlag = vernacularLanguageValidator.evaluate(attribute);
		results.addResult(vernacularLanguageValidator, validationResultFlag);
	}

}
