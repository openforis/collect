/**
 * 
 */
package org.openforis.collect.model.validation;

import static org.openforis.collect.model.FieldSymbol.BLANK_ON_FORM;
import static org.openforis.collect.model.FieldSymbol.CONFIRMED;
import static org.openforis.collect.model.FieldSymbol.DASH_ON_FORM;
import static org.openforis.collect.model.FieldSymbol.ILLEGIBLE;

import java.util.List;

import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.validation.Check.Flag;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.state.NodeState;

/**
 * @author M. Togna
 * 
 */
public class CollectValidator extends Validator {

	@Override
	protected ValidationResults validateAttribute(NodeState nodeState) {
		// CollectRecord record = (CollectRecord) attribute.getRecord();
		// NodeState nodeState = record.getNodeState(attribute);
		Attribute<?, ?> attribute = (Attribute<?, ?>) nodeState.getNode();

		CollectValidationResults results = new CollectValidationResults();
		SpecifiedValidator specifiedValidator = new SpecifiedValidator();
		boolean specified = specifiedValidator.evaluate(nodeState);
		results.addResult(attribute, specifiedValidator, specified);

		if (specified || specifiedValidator.getFlag().equals(Flag.WARN)) {
			// continue with other validation results
			boolean isKey = isRecordKey(attribute);
			if (isKey && !isUnique(nodeState, results)) {
				//stop validation
			} else {
				ValidationResults idmResults = super.validateAttribute(nodeState);
				boolean confirmed = isConfirmedValue(attribute);
				List<ValidationResult> errors = idmResults.getErrors();
				for (ValidationResult error : errors) {
					Flag flag = confirmed ? Flag.WARN : Flag.ERROR;
					results.addFailed(error, flag);
				}
				results.addWarnings(idmResults.getWarnings());
				results.addPassed(idmResults.getPassed());
			}
		}
		return results;

	}

	private boolean isUnique(NodeState nodeState, CollectValidationResults results) {
		RecordKeyUniquenessValidator keyValidator = new RecordKeyUniquenessValidator();
		boolean unique = keyValidator.evaluate(nodeState);
		results.addResult(nodeState.getNode(), keyValidator, unique);
		return unique;
	}

	@Override
	protected ValidationResults validateEntity(NodeState nodeState) {
		CollectValidationResults results = new CollectValidationResults();
		Entity entity = (Entity) nodeState.getNode();
		Record record = entity.getRecord();
		if (record.getRootEntity().equals(entity)) {

		}
		ValidationResults idmResults = super.validateEntity(nodeState);
		results.addErrors(idmResults.getErrors());
		results.addWarnings(idmResults.getWarnings());
		results.addPassed(idmResults.getPassed());

		return results;
	}

	private boolean isRecordKey(Attribute<?, ?> attribute) {
		Record record = attribute.getRecord();
		return attribute instanceof KeyAttributeDefinition && record.getRootEntity().equals(attribute.getParent());
	}

	// private CollectValidationResults validateEntryPhase(Attribute<?, ?> attribute, NodeState nodeState) {
	// CollectValidationResults results = new CollectValidationResults();
	// SpecifiedValidator specifiedValidator = new SpecifiedValidator(attribute.getDefinition(), nodeState);
	// boolean specified = specifiedValidator.evaluate(attribute);
	// results.addResult(attribute, specifiedValidator, specified);
	//
	// if(specified || specifiedValidator.getFlag().equals(Flag.WARN)){
	// //continue with other validations
	// ValidationResults idmResults = super.validate(attribute);
	// boolean confirmed = isConfirmedValue(attribute);
	// List<ValidationResult> errors = idmResults.getErrors();
	// for (ValidationResult error : errors) {
	// Flag flag = confirmed ? Flag.WARN : Flag.ERROR;
	// results.addFailed(error, flag);
	// }
	// results.addWarnings(idmResults.getWarnings());
	// results.addPassed(idmResults.getPassed());
	// }
	// return results;
	// }
	//
	// private ValidationResults validateCleansingPhase(Attribute<?, ?> attribute) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// private ValidationResults validateAnalysisPhase(Attribute<?, ?> attribute) {
	// return super.validate(attribute);
	// }

	static boolean notReasonBlankSpecified(Attribute<?, ?> attribute) {
		int fieldCount = attribute.getFieldCount();
		for (int i = 0; i < fieldCount; i++) {
			Field<?> field = attribute.getField(i);
			Character symbol = field.getSymbol();

			if (!(ILLEGIBLE.getSymbol().equals(symbol) || BLANK_ON_FORM.getSymbol().equals(symbol) || DASH_ON_FORM.getSymbol().equals(symbol))) {
				return true;
			}
		}
		return false;
	}

	static boolean isConfirmedValue(Attribute<?, ?> attribute) {
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
