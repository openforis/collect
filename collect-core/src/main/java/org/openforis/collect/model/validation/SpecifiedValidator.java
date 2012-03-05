/**
 * 
 */
package org.openforis.collect.model.validation;

import static org.openforis.collect.model.FieldSymbol.BLANK_ON_FORM;
import static org.openforis.collect.model.FieldSymbol.DASH_ON_FORM;
import static org.openforis.collect.model.FieldSymbol.ILLEGIBLE;
import static org.openforis.idm.metamodel.validation.ValidationResultFlag.ERROR;
import static org.openforis.idm.metamodel.validation.ValidationResultFlag.OK;
import static org.openforis.idm.metamodel.validation.ValidationResultFlag.WARNING;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationRule;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;

/**
 * @author M. Togna
 * @author G. Miceli
 * @author S. Ricci
 */
public class SpecifiedValidator implements ValidationRule<Attribute<?,?>> {

	@Override
	public ValidationResultFlag evaluate(Attribute<?, ?> attribute) {
		CollectRecord record = (CollectRecord) attribute.getRecord();
		Step step = record.getStep();
		if ( Step.ENTRY == step ) {
			if ( isRelevant(attribute) && attribute.isEmpty() ) {
				if ( isReasonBlankAlwaysSpecified(attribute) ) {
					if (isRequired(attribute)) {
						return WARNING;
					} else {
						return OK;
					}
				} else {
					return ERROR;
				}	
			} else {
				return OK;
			}
		} else {
			return OK;
		}
	}

	private boolean isReasonBlankAlwaysSpecified(Attribute<?, ?> attribute) {
		int fieldCount = attribute.getFieldCount();
		for (int i = 0; i < fieldCount; i++) {
			Field<?> field = attribute.getField(i);
			Character symbolCode = field.getSymbol();
			FieldSymbol symbol = FieldSymbol.valueOf(symbolCode);
			if ( !(symbol == BLANK_ON_FORM || symbol == DASH_ON_FORM || symbol == ILLEGIBLE) ) {
				return false;
			}
		}
		return true;
	}

	private boolean isRequired(Attribute<?, ?> attribute) {
		Entity parent = attribute.getParent();
		return parent.isRequired(attribute.getName());
	}

	private boolean isRelevant(Attribute<?, ?> attribute) {
		Entity parent = attribute.getParent();
		return parent.isRelevant(attribute.getName());
	}
}
