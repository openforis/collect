/**
 * 
 */
package org.openforis.collect.model.validation;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationRule;
import org.openforis.idm.model.Attribute;

/**
 * @author M. Togna
 * 
 */
public class RecordKeyUniquenessValidator implements ValidationRule<Attribute<?, ?>> {

	private RecordManager recordManager;
	
	public RecordKeyUniquenessValidator(RecordManager recordManager) {
		this.recordManager = recordManager;
	}

	@Override
	public ValidationResultFlag evaluate(Attribute<?, ?> node) {
		CollectRecord record = (CollectRecord) node.getRecord();
		if (record.isIgnoreDuplicateRecordKeyValidationErrors() 
				|| record.getSurvey().getId() == null) {
			return ValidationResultFlag.OK;
		} else {
			boolean unique = recordManager.isUnique(record);
			return ValidationResultFlag.valueOf(unique);
		}
	}

}
