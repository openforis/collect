/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.model.CodeAttribute;

/**
 * @author M. Togna
 * 
 *         Checks if code is valid in context of parent (if parent scope in IDM, not list scope!)
 *         <p/>
 *         Generates a warning if parent is invalid or missing.
 */
public class CodeParentValidator implements ValidationRule<CodeAttribute> {

	@Override
	public ValidationResultFlag evaluate(CodeAttribute node) {

		CodeAttributeDefinition definition = node.getDefinition();
		if ( StringUtils.isBlank(definition.getParentExpression()) ) {
			return ValidationResultFlag.OK;
		} else {
			CodeAttribute parentCode = node.getCodeParent();
			if ( parentCode == null ) {
				return ValidationResultFlag.WARNING;
			} else if ( isExternalCodeList(parentCode) ) {
				ExternalCodeValidator externalCodeValidator = new ExternalCodeValidator();
				ValidationResultFlag parentResult = externalCodeValidator.evaluate(parentCode);
				if ( parentResult == ValidationResultFlag.ERROR ) {
					return ValidationResultFlag.WARNING;
				} else {
					return ValidationResultFlag.OK;
				}
			} else {
				CodeListItem parentCodeListItem = getCodeListItem(parentCode);
				if ( parentCodeListItem == null ) {
					return ValidationResultFlag.WARNING;
				}
				return ValidationResultFlag.OK;
			}
		}
	}

	protected CodeListItem getCodeListItem(CodeAttribute parentCode) {
		@SuppressWarnings("deprecation")
		CodeListItem parentCodeListItem = parentCode.getCodeListItem();
		return parentCodeListItem;
	}

	private boolean isExternalCodeList(CodeAttribute node) {
		CodeAttributeDefinition definition = node.getDefinition();
		CodeList codeList = definition.getList();
		return codeList.isExternal();
	}

}
