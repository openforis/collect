/**
 * 
 */
package org.openforis.collect.model.validation;

import static org.openforis.collect.model.FieldSymbol.CONFIRMED;

import java.util.List;

import org.openforis.collect.manager.RecordManager;
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
		SpecifiedValidator specifiedValidator = new SpecifiedValidator();
		ValidationResultFlag specified = specifiedValidator.evaluate(attribute);
		results.addResult(specifiedValidator, specified);

		if ( !specified.isError() ) {
			boolean isKey = isRecordKey(attribute);
			if (isKey) {
				RecordKeyUniquenessValidator keyValidator = new RecordKeyUniquenessValidator(recordManager);
				ValidationResultFlag res = keyValidator.evaluate(attribute);
				if(res == ValidationResultFlag.ERROR){
					results.addResult(keyValidator, ValidationResultFlag.ERROR);
				}
			}
			// TODO only do in phase 1
			// Lower error level of confirmed error values				
			ValidationResults idmResults = super.validate(attribute);
			boolean confirmed = isValueConfirmed(attribute);
			List<ValidationResult> errors = idmResults.getErrors();
			for (ValidationResult error : errors) {
				ValidationResultFlag newFlag = confirmed ? ValidationResultFlag.WARNING : ValidationResultFlag.ERROR;
				results.addResult(error.getValidator(), newFlag);
			}
			results.addResults(idmResults.getWarnings());
		}
		return results;

	}

	private boolean isRecordKey(Attribute<?, ?> attribute) {
		Record record = attribute.getRecord();
		return attribute.getDefinition() instanceof KeyAttributeDefinition && record.getRootEntity().equals(attribute.getParent());
	}

	private boolean isValueConfirmed(Attribute<?, ?> attribute) {
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
}
