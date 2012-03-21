/**
 * 
 */
package org.openforis.collect.model.validation;

import static org.openforis.collect.model.FieldSymbol.CONFIRMED;

import java.util.List;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Field;
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

		// check if attribute has been specified
		SpecifiedValidator specifiedValidator = new SpecifiedValidator();
		ValidationResultFlag specifiedResultFlag = specifiedValidator.evaluate(attribute);
		results.addResult(specifiedValidator, specifiedResultFlag);

		if ( !specifiedResultFlag.isError() ) {
			CollectRecord record = (CollectRecord) attribute.getRecord();
			Step step = record.getStep();

			// validate root entity keys
			if ( isRootEntityKey(attribute) ) {
				validateRootEntityKey(attribute, results);
			}

			/*
			 * if the attribute is not empty and 'reason blank' has not been specified than validate it
			 */
			if (!(attribute.isEmpty() || isReasonBlankSpecified(attribute))) {
				validateAttributeValue(attribute, results);
				if (!results.hasErrors()) {
					validateAttributeChecks(attribute, results);
				}
			}

			if (step == Step.ENTRY) {
				results = adjustErrorsForEntryPhase(results, attribute);
			}
		}
		return results;
	}

	private ValidationResults adjustErrorsForEntryPhase(ValidationResults results, Attribute<?, ?> attribute) {
		boolean confirmed = isValueConfirmed(attribute);
		
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
		return attribute.getDefinition() instanceof KeyAttributeDefinition && record.getRootEntity().equals(attribute.getParent());
	}

	static boolean isValueConfirmed(Attribute<?, ?> attribute) {
		int fieldCount = attribute.getFieldCount();
		for (int i = 0; i < fieldCount; i++) {
			Field<?> field = attribute.getField(i);
			Character symbol = field.getSymbol();

			if (!CONFIRMED.getSymbol().equals(symbol)) {
				return false;
			}
		}
		return true;
	}

	static boolean isReasonBlankSpecified(Attribute<?, ?> attribute) {
		int fieldCount = attribute.getFieldCount();
		for (int i = 0; i < fieldCount; i++) {
			Field<?> field = attribute.getField(i);
			Character character = field.getSymbol();
			FieldSymbol fieldSymbol = FieldSymbol.valueOf(character);
			if (fieldSymbol == null || !fieldSymbol.isReasonBlank()) {
				return false;
			}
		}
		return true;
	}
}
