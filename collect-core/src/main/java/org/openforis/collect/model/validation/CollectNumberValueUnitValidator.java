/**
 * 
 */
package org.openforis.collect.model.validation;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.idm.metamodel.validation.NumberValueUnitValidator;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.model.NumberAttribute;

/**
 * @author S. Ricci
 *
 */
public class CollectNumberValueUnitValidator extends NumberValueUnitValidator {

	@Override
	public ValidationResultFlag evaluate(NumberAttribute<?, ?> attribute) {
		CollectRecord record = (CollectRecord) attribute.getRecord();
		Step step = record.getStep();
		ValidationResultFlag resultFlag = super.evaluate(attribute);
		if ( resultFlag == ValidationResultFlag.ERROR && step == Step.ENTRY ) {
			Character unitSymbolChar = attribute.getUnitField().getSymbol();
			FieldSymbol unitSymbol = FieldSymbol.valueOf(unitSymbolChar);
			if ( unitSymbol != null && unitSymbol.isReasonBlank() ) {
				return ValidationResultFlag.WARNING;
			}
		}
		return resultFlag;
	}
}
