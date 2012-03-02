/**
 * 
 */
package org.openforis.collect.model.validation;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;

/**
 * @author M. Togna
 * 
 */
public class SpecifiedValidator extends Check<Attribute<?, ?>> {

	private static final long serialVersionUID = 1L;

	private Flag flag;

	public SpecifiedValidator() {
		flag = Flag.ERROR;
	}

	@Override
	public boolean evaluate(Attribute<?, ?> attribute) {
		CollectRecord record = (CollectRecord) attribute.getRecord();
		Step step = record.getStep();
		if (isRelevant(attribute) && attribute.isEmpty()) {
			if (Step.ENTRY.equals(step)) {
				if (CollectValidator.notReasonBlankSpecified(attribute)) {
					flag = Flag.ERROR;
				} else if (isRequired(attribute)) {
					flag = Flag.WARN;
				}
			} else {
				flag = Flag.ERROR;
			}
			return false;
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

	@Override
	public Flag getFlag() {
		return flag;
	}

}
