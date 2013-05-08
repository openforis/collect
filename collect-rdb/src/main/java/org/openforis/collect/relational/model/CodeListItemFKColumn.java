/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.path.Path;

/**
 * @author S. Ricci
 *
 */
public class CodeListItemFKColumn extends DataColumn {

	CodeListItemFKColumn(String name, CodeAttributeDefinition defn, Path relPath) {
		super(name, Types.BIGINT, "bigint", defn, relPath, null, true);
	}

	@Override
	public Object extractValue(Node<?> context) {
		NodeDefinition defn = getNodeDefinition();
		if ( defn instanceof CodeAttributeDefinition ) {
			Node<?> valNode = super.extractValueNode(context);
			if ( valNode != null && valNode instanceof CodeAttribute ) {
				CodeAttribute codeAttr = (CodeAttribute) valNode;
				if ( codeAttr.getValue() != null ) { 
					CodeListItem codeListItem = codeAttr.getCodeListItem();
					return codeListItem == null ? null: codeListItem.getId();
				} else {
					return getDefaultCodeId(defn);
				}
			} else {
				return getDefaultCodeId(defn);
			}
		}
		return null;
	}

	protected Object getDefaultCodeId(NodeDefinition defn) {
		CodeList list = ((CodeAttributeDefinition) defn).getList();
		return CodeListPrimaryKeyColumn.getDefaultCodeId(list);
	}

}
