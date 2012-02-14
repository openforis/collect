package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.ModelVersionUtil;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.IdmInterpretationError;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidPathException;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.springframework.beans.factory.annotation.Autowired;

public class CodeListManager {
	
	@Autowired
	private ExpressionFactory expressionFactory;
	
	public List<CodeListItem> findCodeList(Entity parentEntity, CodeAttributeDefinition def, ModelVersion version) {
		CodeAttribute parent = findParent(parentEntity, def);
		List<CodeListItem> items;
		if(parent == null) {
			//node is root
			CodeList list = def.getList();
			items = list.getItems();
		} else {
			Entity ancestorEntity = parent.getParent();
			CodeAttributeDefinition parentDefinition = parent.getDefinition();
			List<CodeListItem> codeList = findCodeList(ancestorEntity, parentDefinition, version);
			Code parentCode = parent.getValue();
			String parentCodeValue = parentCode.getCode();
			CodeListItem parentItem = getCodeListItem(codeList, parentCodeValue);
			items = parentItem.getChildItems();
		}
		List<CodeListItem> itemsInVersion = new ArrayList<CodeListItem>();
		for (CodeListItem codeListItem : items) {
			if (ModelVersionUtil.isInVersion(codeListItem, version)) {
				itemsInVersion.add(codeListItem);
			}
		}
		return itemsInVersion;
	}
	
	
	/**
	 * Apply the parentExpression in the attribute definition to the parentEntity specified
	 * 
	 * @param parentEntity
	 * @param def
	 * @return
	 */
	private CodeAttribute findParent(Entity parentEntity, CodeAttributeDefinition def) {
		String parentExpression = def.getParentExpression();
		if(StringUtils.isNotBlank(parentExpression)) {
			ModelPathExpression expression = expressionFactory.createModelPathExpression(parentExpression);
			Object result;
			try {
				result = expression.evaluate(parentEntity);
				if(result instanceof CodeAttribute) {
					return (CodeAttribute) result;
				} else {
					throw new IdmInterpretationError("CodeAttribute exptected");
				}
			} catch (InvalidPathException e) {
				throw new IdmInterpretationError("Error retrieving parent code", e);
			}
		} else {
			return null;
		}
	}
	
	private CodeListItem getCodeListItem(List<CodeListItem> siblings, String code) {
		for (CodeListItem item : siblings) {
			String itemCode = item.getCode();
			String paddedCode;
			if (itemCode.length() > code.length()) {
				//try to left pad the code with '0'
				paddedCode = StringUtils.leftPad(code, itemCode.length(), '0');
			} else {
				paddedCode = code;
			}
			if (itemCode.equalsIgnoreCase(paddedCode)) {
				return item;
			}
		}
		return null;
	}
	
	private Code parseCode(String value, List<CodeListItem> codeList, ModelVersion version) {
		Code code = null;
		String[] strings = value.split(":");
		String codeStr = strings[0].trim();
		String qualifier = null;
		if(strings.length == 2) {
			qualifier = strings[1].trim();
		}
		if(codeList != null) {
			CodeListItem codeListItem = getCodeListItem(codeList, codeStr);
			if(codeListItem != null) {
				code = new Code(codeListItem.getCode(), qualifier);
				return code;
			}
		}
		if (code == null) {
			code = new Code(codeStr, qualifier);
		}
		return code;
	}
	
	public List<Code> parseCodes(Entity parentEntity, CodeAttributeDefinition def, String value, ModelVersion version) {
		List<Code> result = new ArrayList<Code>();
		StringTokenizer st = new StringTokenizer(value, ",");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			List<CodeListItem> codeList = null;
			if(def.getList() != null) {
				codeList = findCodeList(parentEntity, def, version);
			}
			Code code = parseCode(token, codeList, version);
			if(code != null) {
				result.add(code);
			} else {
				//TODO throw exception
			}
		}
		return result;
	}

}
