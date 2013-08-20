package org.openforis.collect.model.validation;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.validation.CodeValidator;
import org.openforis.idm.model.CodeAttribute;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectCodeValidator extends CodeValidator {

	private CodeListManager codeListManager;

	public CollectCodeValidator(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
	@Override
	protected CodeListItem getCodeListItem(CodeAttribute attribute) {
		return codeListManager.loadItemByAttribute(attribute);
	}
	
}
