/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import static org.openforis.idm.metamodel.validation.ValidationResultFlag.ERROR;
import static org.openforis.idm.metamodel.validation.ValidationResultFlag.OK;

import org.openforis.idm.metamodel.Calculable;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;

/**
 * @author M. Togna
 * @author G. Miceli
 */
public class MaxCountValidator implements ValidationRule<Entity> {

	private NodeDefinition nodeDefinition;
	
	public MaxCountValidator(NodeDefinition nodeDefinition) {
		this.nodeDefinition = nodeDefinition;
	}

	public NodeDefinition getNodeDefinition() {
		return nodeDefinition;
	}
	
	@Override
	public ValidationResultFlag evaluate(Entity entity) {
		if ( nodeDefinition instanceof Calculable && ((Calculable) nodeDefinition).isCalculated() ) {
			return OK;
		}
		Integer maxCount = entity.getMaxCount(nodeDefinition);
		if (maxCount == null) {
			return OK;
		} else {
			String childName = nodeDefinition.getName();
			int count = entity.getCount(childName);
			if ( count <= maxCount ) {
				return OK;
			} else {
				return ERROR;
			}
		}
	}
}
