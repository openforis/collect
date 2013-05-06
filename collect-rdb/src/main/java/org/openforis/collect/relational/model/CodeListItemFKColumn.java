/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
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
		Node<?> valNode = super.extractValueNode(context);
		if ( valNode != null && valNode instanceof CodeAttribute ) {
			CodeListItem codeListItem = ((CodeAttribute) valNode).getCodeListItem();
			if ( codeListItem != null ) {
				return codeListItem.getId();
			}
		}
		return null;
	}

}
