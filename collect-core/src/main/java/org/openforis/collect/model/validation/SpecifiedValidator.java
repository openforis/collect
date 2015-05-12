/**
 * 
 */
package org.openforis.collect.model.validation;
import static org.openforis.collect.model.validation.CollectValidator.isReasonBlankAlwaysSpecified;
import static org.openforis.idm.metamodel.validation.ValidationResultFlag.ERROR;
import static org.openforis.idm.metamodel.validation.ValidationResultFlag.OK;
import static org.openforis.idm.metamodel.validation.ValidationResultFlag.WARNING;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationRule;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.FileAttribute;

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
			if ( attribute.isRelevant() && attribute.isEmpty() && ! (attribute instanceof FileAttribute) ) {
				if ( isReasonBlankAlwaysSpecified(attribute) ) {
					if ( attribute.isRequired() ) {
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
		}
		
		return OK;
	}

}
