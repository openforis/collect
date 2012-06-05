/**
 * 
 */
package org.openforis.collect.model.validation;

import java.util.List;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.MinCountValidator;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.Attribute;
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
			record.updateValidationCounts(attribute.getInternalId(), results);
		}
		return results;
	}

	@Override
	protected MinCountValidator getMinCountValidator(NodeDefinition defn) {
		return new CollectMinCountValidator(defn);
	}
	
	@Override
	public ValidationResultFlag validateMinCount(Entity entity, String childName) {
		ValidationResultFlag flag = super.validateMinCount(entity, childName);
		CollectRecord record = (CollectRecord) entity.getRecord();
		record.updateValidationMinCounts(entity.getInternalId(), childName, flag);
		return flag;
	}
	
	@Override
	public ValidationResultFlag validateMaxCount(Entity entity, String childName) {
		ValidationResultFlag flag = super.validateMaxCount(entity, childName);
		CollectRecord record = (CollectRecord) entity.getRecord();
		record.updateValidationMaxCounts(entity.getInternalId(), childName, flag);
		return flag;
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
		if ( attribute instanceof NumberAttribute ) {
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
}
