/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.model.CodeAttribute;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class CodeValidator implements ValidationRule<CodeAttribute> {

	@Override
	public ValidationResultFlag evaluate(CodeAttribute attribute) {
		if ( attribute.isExternalCodeList() ) {
			ExternalCodeValidator externalCodeValidator = new ExternalCodeValidator();
			return externalCodeValidator.evaluate(attribute);
		} else {
			CodeListItem item = getCodeListItem(attribute);
			if (item == null) {
				if (isAllowedUnlisted(attribute)) {
					return ValidationResultFlag.WARNING;
				} else {
					return ValidationResultFlag.ERROR;
				}
			} else {
				return ValidationResultFlag.OK;
			}
		}
	}

	@SuppressWarnings("deprecation")
	protected CodeListItem getCodeListItem(CodeAttribute attribute) {
		return attribute.getCodeListItem();
	}

	private boolean isAllowedUnlisted(CodeAttribute attribute) {
		CodeAttributeDefinition definition = attribute.getDefinition();
		return definition.isAllowUnlisted();
	}

}
