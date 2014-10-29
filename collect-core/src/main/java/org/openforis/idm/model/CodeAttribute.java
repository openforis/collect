package org.openforis.idm.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.ModelPathExpression;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class CodeAttribute extends Attribute<CodeAttributeDefinition, Code> {

	private static final long serialVersionUID = 1L;
	
	public CodeAttribute(CodeAttributeDefinition definition) {
		super(definition);
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
	 * @deprecated Access code list items using manager class.
	 * 
	 * @return
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
		try {
			String parentExpr = definition.getParentExpression();
			if (StringUtils.isBlank(parentExpr)) {
				return null;
			}
			SurveyContext recordContext = getRecord().getSurveyContext();
			ExpressionFactory expressionFactory = recordContext.getExpressionFactory();
			ModelPathExpression expression = expressionFactory.createModelPathExpression(parentExpr);
			Node<?> parentNode = expression.evaluate(getParent(), this);
			if (parentNode != null && parentNode instanceof CodeAttribute) {
				return (CodeAttribute) parentNode;
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException("Error while getting parent code " + e);
		}
	}
	
	/**
	 * Returns a list of ancestors CodeAttribute objects, starting from the root.
	 * It is applicable only to hierarchical code lists.
	 * 
	 * @return
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

}
