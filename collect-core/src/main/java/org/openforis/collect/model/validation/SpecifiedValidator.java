/**
 * 
 */
package org.openforis.collect.model.validation;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.state.NodeState;

/**
 * @author M. Togna
 * 
 */
public class SpecifiedValidator extends Check {

	private static final long serialVersionUID = 1L;

	private Flag flag;

	public SpecifiedValidator() {
		flag = Flag.ERROR;
	}

	@Override
	public boolean evaluate(NodeState nodeState) {
		Attribute<?, ?> attribute = (Attribute<?, ?>) nodeState.getNode();
		CollectRecord record = (CollectRecord) attribute.getRecord();
		Step step = record.getStep();

		if (nodeState.isRelevant() && attribute.isEmpty()) {
			if (Step.ENTRY.equals(step)) {
				if (CollectValidator.notReasonBlankSpecified(attribute)) {
					flag = Flag.ERROR;
				} else if (nodeState.isRequired()) {
					flag = Flag.WARN;
				}
			} else {
				flag = Flag.ERROR;
			}
			return false;
		}
		return true;
	}

	@Override
	public Flag getFlag() {
		return flag;
	}

}
