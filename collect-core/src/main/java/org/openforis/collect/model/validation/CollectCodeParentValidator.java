/**
 * 
 */
package org.openforis.collect.model.validation;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.validation.CodeParentValidator;
import org.openforis.idm.model.CodeAttribute;

/**
 * @author S. Ricci
 *
 */
public class CollectCodeParentValidator extends CodeParentValidator {

	private CodeListManager codeListManager;
	
	public CollectCodeParentValidator(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
	@Override
	protected CodeListItem getCodeListItem(CodeAttribute attr) {
		return codeListManager.loadItemByAttribute(attr);
	}
	
}
