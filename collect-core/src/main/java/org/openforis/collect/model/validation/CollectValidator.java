/**
 * 
 */
package org.openforis.collect.model.validation;

import java.util.List;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.CodeParentValidator;
import org.openforis.idm.metamodel.validation.CodeValidator;
import org.openforis.idm.metamodel.validation.MinCountValidator;
import org.openforis.idm.metamodel.validation.NumberValueUnitValidator;
import org.openforis.idm.metamodel.validation.NumericRangeUnitValidator;
import org.openforis.idm.metamodel.validation.TaxonVernacularLanguageValidator;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.NumericRangeAttribute;
import org.openforis.idm.model.Record;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class CollectValidator extends Validator {

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private CodeListManager codeListManager;

	@Override
	public ValidationResults validate(Attribute<?, ?> attribute) {
		ValidationResults results = new ValidationResults();

		CollectRecord record = (CollectRecord) attribute.getRecord();

		// check if attribute has been specified
		SpecifiedValidator specifiedValidator = new SpecifiedValidator();
		ValidationResultFlag specifiedResultFlag = specifiedValidator.evaluate(attribute);
		results.addResult(specifiedValidator, specifiedResultFlag);

		if ( specifiedResultFlag.isError() ) {
			record.updateSkippedCount(attribute.getInternalId());
		} else {
			Step step = record.getStep();

			// validate root entity keys
			if ( isRootEntityKey(attribute) ) {
				validateRootEntityKey(attribute, results);
			}

			/*
			 * if the attribute is not empty and 'reason blank' has not been specified than validate it
			 */
			if (!(attribute.isEmpty() || isReasonBlankAlwaysSpecified(attribute))) {
				validateAttributeValue(attribute, results);
				if (!results.hasErrors()) {
					validateAttributeChecks(attribute, results);
				}
			}

			if (step == Step.ENTRY) {
				results = adjustErrorsForEntryPhase(results, attribute);
			}
			record.updateAttributeValidationCache(attribute.getInternalId(), results);
		}
		return results;
	}

	@Override
	protected MinCountValidator getMinCountValidator(NodeDefinition defn) {
		return new CollectMinCountValidator(defn);
	}
	
	@Override
	protected TaxonVernacularLanguageValidator getTaxonVernacularLanguageValidator() {
		return new CollectTaxonVernacularLanguageValidator();
	}
	
	@Override
	protected CodeValidator getCodeValidator() {
		return new CollectCodeValidator(codeListManager);
	}
	
	@Override
	protected CodeParentValidator getCodeParentValidator() {
		return new CollectCodeParentValidator(codeListManager);
	}
	
	@Override
	public ValidationResultFlag validateMinCount(Entity entity, String childName) {
		ValidationResultFlag flag = super.validateMinCount(entity, childName);
		CollectRecord record = (CollectRecord) entity.getRecord();
		record.updateMinCountsValidationCache(entity.getInternalId(), childName, flag);
		return flag;
	}
	
	@Override
	public ValidationResultFlag validateMaxCount(Entity entity, String childName) {
		ValidationResultFlag flag = super.validateMaxCount(entity, childName);
		CollectRecord record = (CollectRecord) entity.getRecord();
		record.updateMaxCountsValidationCache(entity.getInternalId(), childName, flag);
		return flag;
	}
	
	@Override
	protected void validateNumericAttributeUnit(
			NumberAttribute<?, ?> attribute, ValidationResults results) {
		NumberValueUnitValidator validator = new CollectNumberValueUnitValidator();
		ValidationResultFlag result = validator.evaluate(attribute);
		results.addResult(validator, result);
	}
	
	@Override
	protected void validateNumericRangeUnit(
			NumericRangeAttribute<?, ?> attribute, ValidationResults results) {
		NumericRangeUnitValidator unitValidator = new CollectNumericRangeUnitValidator();
		ValidationResultFlag unitValidationResult = unitValidator.evaluate(attribute);
		results.addResult(unitValidator, unitValidationResult);
	}
	
	private ValidationResults adjustErrorsForEntryPhase(ValidationResults results, Attribute<?, ?> attribute) {
		boolean confirmed = isErrorConfirmed(attribute);
		
		ValidationResults phaseEntryResults = new ValidationResults();
		List<ValidationResult> errors = results.getErrors();
		for (ValidationResult error : errors) {
			ValidationResultFlag newFlag = confirmed ? ValidationResultFlag.WARNING : ValidationResultFlag.ERROR;
			phaseEntryResults.addResult(error.getValidator(), newFlag);
		}
		
		phaseEntryResults.addResults(results.getWarnings());
		return phaseEntryResults;
	}

	private void validateRootEntityKey(Attribute<?, ?> attribute, ValidationResults results) {
		RecordKeyUniquenessValidator keyValidator = new RecordKeyUniquenessValidator(recordManager);
		ValidationResultFlag res = keyValidator.evaluate(attribute);
		if (res == ValidationResultFlag.ERROR) {
			results.addResult(keyValidator, ValidationResultFlag.ERROR);
		}
	}

	private boolean isRootEntityKey(Attribute<?, ?> attribute) {
		Record record = attribute.getRecord();
		return attribute.getDefinition() instanceof KeyAttributeDefinition &&
				((KeyAttributeDefinition) attribute.getDefinition()).isKey() &&
				record.getRootEntity().equals(attribute.getParent()
				);
	}

	static boolean isErrorConfirmed(Attribute<?, ?> attribute) {
		CollectRecord record = (CollectRecord) attribute.getRecord();
		return record.isErrorConfirmed(attribute);
	}

	static boolean isReasonBlankAlwaysSpecified(Attribute<?, ?> attribute) {
		int fieldCount = 0;
		// ignore unit for numeric attributes
		if ( attribute instanceof NumberAttribute || attribute instanceof CodeAttribute ) {
			fieldCount = 1;
		} else if ( attribute instanceof NumericRangeAttribute ) {
			fieldCount = 2;
		} else {
			fieldCount = attribute.getFieldCount();
		}

		for ( int i = 0 ; i < fieldCount ; i++ ) {
			Field<?> field = attribute.getField(i);
			Character symbolCode = field.getSymbol();
			FieldSymbol symbol = FieldSymbol.valueOf(symbolCode);
			if ( symbol == null || !symbol.isReasonBlank() ) {
				return false;
			}
		}
		return true;
	}
	
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	
	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
}
