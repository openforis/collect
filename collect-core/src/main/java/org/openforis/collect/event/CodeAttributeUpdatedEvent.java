package org.openforis.collect.event;

import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.model.Code;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class CodeAttributeUpdatedEvent extends AttributeValueUpdatedEvent<Code> {

	private CodeListItem codeListItem;

	public CodeListItem getCodeListItem() {
		return codeListItem;
	}
	
	public void setCodeListItem(CodeListItem item) {
		this.codeListItem = item;
	}

}
