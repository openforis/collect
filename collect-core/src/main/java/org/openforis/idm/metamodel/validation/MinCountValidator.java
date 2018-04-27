/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import static org.openforis.idm.metamodel.validation.ValidationResultFlag.ERROR;
import static org.openforis.idm.metamodel.validation.ValidationResultFlag.OK;

import java.util.List;

import org.openforis.idm.metamodel.Calculable;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author M. Togna
 * @author G. Miceli
 */
public class MinCountValidator implements ValidationRule<Entity> {

	private NodeDefinition nodeDefinition;

	public MinCountValidator(NodeDefinition nodeDefinition) {
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
		if( entity.isRelevant(nodeDefinition) ) {
			int minCount = entity.getMinCount(nodeDefinition);
			if ( minCount == 0 ) {
				return OK;
			} else {
				int nonEmptyCount = 0;
				List<Node<?>> childNodes = entity.getChildren(nodeDefinition);
				for ( Node<?> child : childNodes ) {
					if ( !isEmpty(child) ) {
						nonEmptyCount++;
						if ( nonEmptyCount >= minCount ) {
							return OK;
						}
					}
				}
				if (nodeDefinition instanceof CodeAttributeDefinition && ! isAvailableCodeListItems(entity)) {
					return OK;
				} else {
					return ERROR;
				}
			}
		} else {
			return OK;
		}
	}
	
	protected boolean isEmpty(Node<?> node){
		return node.isEmpty();
	}
	
	private boolean isAvailableCodeListItems(Entity parentEntity) {
		CodeAttributeDefinition codeAttrDef = (CodeAttributeDefinition) nodeDefinition;
		SurveyContext context = codeAttrDef.getSurvey().getContext();
		CodeListService codeListService = context.getCodeListService();
		if (codeListService == null) {
			//test context does not have a CodeListService
			return true;
		}
		List<CodeListItem> validItems = codeListService.loadValidItems(parentEntity, codeAttrDef);
		return ! validItems.isEmpty();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append("('");
		sb.append(nodeDefinition.getName());
		sb.append("')");
		return sb.toString();
	}
}
