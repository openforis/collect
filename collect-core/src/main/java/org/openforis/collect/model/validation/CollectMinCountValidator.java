/**
 * 
 */
package org.openforis.collect.model.validation;

import static org.openforis.collect.model.validation.CollectValidator.isReasonBlankSpecified;

import java.util.List;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.MinCountValidator;
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
		return attribute.isEmpty() && !isReasonBlankSpecified(attribute);
	}

}
