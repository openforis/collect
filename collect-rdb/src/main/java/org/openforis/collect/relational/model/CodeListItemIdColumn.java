/**
 * 
 */
package org.openforis.collect.relational.model;

import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.model.CodeAttribute;

/**
 * @author S. Ricci
 *
 */
public class CodeListItemIdColumn extends IdColumn<CodeAttribute> {

	CodeListItemIdColumn(String name) {
		super(name);
	}

	@Override
	public Object extractValue(CodeAttribute source) {
		CodeListItem codeListItem = source.getCodeListItem();
		if ( codeListItem != null ) {
			return codeListItem.getId();
		} else {
			return null;
		}
	}
	
	

}
