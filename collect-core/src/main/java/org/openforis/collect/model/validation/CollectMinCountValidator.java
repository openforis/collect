package org.openforis.collect.model.validation;

import static org.openforis.collect.model.validation.CollectValidator.isReasonBlankAlwaysSpecified;

import java.util.List;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.MinCountValidator;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author M. Togna
 * 
 */
public class CollectMinCountValidator extends MinCountValidator {

	public CollectMinCountValidator(NodeDefinition nodeDefinition) {
		super(nodeDefinition);
	}

	@Override
	public ValidationResultFlag evaluate(Entity entity) {
		CollectRecord record = (CollectRecord) entity.getRecord();
		
		ValidationResultFlag resultFlag = super.evaluate(entity);
		
		// you can approve missing values in entry phase as well
		if ( resultFlag == ValidationResultFlag.ERROR ) {
			if (record.getStep() == Step.CLEANSING && record.isMissingApproved(entity, getNodeDefinition())) {
				resultFlag = ValidationResultFlag.WARNING;
			}
		}
		return resultFlag;
	}
	
	@Override
	protected boolean isEmpty(Node<?> node) {
		if (node instanceof Entity) {
			return isEmpty((Entity) node);
		} else {
			return isEmpty((Attribute<?, ?>) node);
		}
	}

	protected boolean isEmpty(Entity entity) {
		List<Node<?>> children = entity.getChildren();
		for (Node<?> child : children) {
			if (!isEmpty(child)) {
				return false;
			}
		}
		return true;
	}

	protected boolean isEmpty(Attribute<?, ?> attribute) {
		CollectRecord record = (CollectRecord) attribute.getRecord();
		Step step = record.getStep();

		if ( isReasonBlankAlwaysSpecified(attribute) ) {
			if ( step == Step.ENTRY ) {
				return false;
			} else {
				return true;
			}
		} else {
			return attribute.isEmpty();
		}
	}

}
