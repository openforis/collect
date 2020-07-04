package org.openforis.idm.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class CodeAttribute extends Attribute<CodeAttributeDefinition, Code> {

	private static final long serialVersionUID = 1L;
	
	public CodeAttribute(CodeAttributeDefinition definition) {
		super(definition);
	}

	@Override
	public boolean isUserSpecified() {
		return ! ( getDefinition().isCalculated() || isEnumerator());
	}
	
	@SuppressWarnings("unchecked")
	public Field<String> getCodeField() {
		return (Field<String>) getField(0);
	}
	
	@SuppressWarnings("unchecked")
	public Field<String> getQualifierField() {
		return (Field<String>) getField(1);
	}

	@Override
	protected boolean calculateAllFieldsFilled() {
		return getCodeField().hasValue();
	}
	
	@Override
	public Code getValue() {
		String code = getCodeField().getValue();
		String qualifier = getQualifierField().getValue();
		return new Code(code, qualifier);
	}
	
	@Override
	protected void setValueInFields(Code value) {
		String code = value.getCode();
		String qualifier = value.getQualifier();
		getCodeField().setValue(code);
		getQualifierField().setValue(qualifier);
	}
	
	/**
	 * @return Related code list item
	 * 
	 * @deprecated Access code list items using manager class.
	 */
	@Deprecated
	public CodeListItem getCodeListItem() {
		Code code = getValue();
		if (code != null) {
			String codeValue = code.getCode();
			if (StringUtils.isNotBlank(codeValue)) {
				ModelVersion currentVersion = getRecord().getVersion();
				CodeAttributeDefinition definition = getDefinition();
				String parentExpression = definition.getParentExpression();
				if (StringUtils.isBlank(parentExpression)) {
					return findCodeListItem(definition.getList().getItems(), codeValue, currentVersion);
				} else {
					CodeAttribute codeParent = getCodeParent();
					if (codeParent != null) {
						CodeListItem codeListItemParent = codeParent.getCodeListItem();
						if (codeListItemParent != null) {
							return findCodeListItem(codeListItemParent.getChildItems(), codeValue, currentVersion);
						}
					}
				}
			}
		}
		return null;
	}
	
	public CodeAttribute getCodeParent() {
		String parentExpr = definition.getParentExpression();
		if (StringUtils.isBlank(parentExpr)) {
			return null;
		}
		CodeAttribute parentNode = getRecord().determineParentCodeAttribute(this);
		return parentNode;
	}
	
	/**
	 * Returns a list of ancestors CodeAttribute objects, starting from the root.
	 * It is applicable only to hierarchical code lists.
	 * 
	 * @return List of ancestors CodeAttribute objects.
	 */
	public List<CodeAttribute> getCodeAncestors() {
		List<CodeAttribute> result = new ArrayList<CodeAttribute>();
		CodeAttribute parent = getCodeParent();
		while (parent != null) {
			result.add(0, parent);
			parent = parent.getCodeParent();
		}
		return Collections.unmodifiableList(result);
	}
	
	public Set<CodeAttribute> getDependentCodeAttributes() {
		Set<CodeAttribute> dependents = getRecord().determineDependentCodeAttributes(this);
		return dependents;
	}

	private CodeListItem findCodeListItem(List<CodeListItem> list, String value, ModelVersion version) {
		for (CodeListItem item : list) {
			if (item.getCode().equals(value) && (version == null || version.isApplicable(item)) ) {
				return item;
			}
		}
		return null;
	}

	public boolean isExternalCodeList() {
		CodeList codeList = definition.getList();
		return codeList.isExternal();
	}

	public boolean isEnumerator() {
		EntityDefinition parentDefinition = (EntityDefinition) definition.getParentDefinition();
		return definition.isKey() 
				&& parentDefinition.isEnumerable() 
				&& parentDefinition.isEnumerate();
	}
}
