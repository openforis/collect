package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.persistence.DatabaseExternalCodeListProvider;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListManager {
	
	@Autowired
	private DatabaseExternalCodeListProvider provider;

	public CodeListItem getCodeListItem(CodeAttribute attribute) {
		CodeAttributeDefinition defn = attribute.getDefinition();
		CodeList list = defn.getList();
		if ( list.isExternal() ) {
			return provider.getItem(attribute);
		} else {
			return attribute.getCodeListItem();
		}
	}

	public ExternalCodeListItem getParentItem(ExternalCodeListItem item) {
		return provider.getParentItem(item);
	}

	public List<ExternalCodeListItem> getRootItems(CodeList list) {
		if ( list.isExternal() ) {
			return provider.getRootItems(list);
		} else {
			return list.getItems();
		}
	}

	public CodeListItem findCodeListItem(List<CodeListItem> siblings, String code) {
		String adaptedCode = code.trim();
		adaptedCode = adaptedCode.toUpperCase();
		//remove initial zeros
		adaptedCode = adaptedCode.replaceFirst("^0+", "");
		adaptedCode = Pattern.quote(adaptedCode);
		Pattern pattern = Pattern.compile("^[0]*" + adaptedCode + "$", Pattern.CASE_INSENSITIVE);

		for (CodeListItem item : siblings) {
			String itemCode = item.getCode();
			Matcher matcher = pattern.matcher(itemCode);
			if(matcher.find()) {
				return item;
			}
		}
		return null;
	}

	public List<CodeListItem> getAssignableCodeListItems(Entity parent, CodeAttributeDefinition def) {
		Record record = parent.getRecord();
		List<? extends CodeListItem> items = null;
		CodeList list = def.getList();
		if ( StringUtils.isEmpty(def.getParentExpression()) ) {
			if ( list.isExternal() ) {
				items = getRootItems(list);
			} else {
				items = list.getItems();
			}
		} else {
			CodeAttribute parentCodeAttribute = getCodeParent(parent, def);
			if ( parentCodeAttribute != null ) {
				if ( list.isExternal() ) {
					ExternalCodeListItem parentCodeListItem = (ExternalCodeListItem) getCodeListItem(parentCodeAttribute);
					if ( parentCodeListItem != null ) {
						items = getChildItems(parentCodeListItem);
					}
				} else {
					CodeListItem parentCodeListItem = parentCodeAttribute.getCodeListItem();
					if(parentCodeListItem == null) {
						//TODO exception if parent not specified
					} else {
						items = parentCodeListItem.getChildItems();
					}
				}
			}
		}
		List<CodeListItem> result = new ArrayList<CodeListItem>();
		if(items != null) {
			ModelVersion version = record.getVersion();
			for (CodeListItem item : items) {
				if (version == null || version.isApplicable(item)) {
					result.add(item);
				}
			}
		}
		return result;
	}
	
	protected CodeAttribute getCodeParent(Entity context, CodeAttributeDefinition def) {
		try {
			Record record = context.getRecord();
			SurveyContext surveyContext = record.getSurveyContext();
			ExpressionFactory expressionFactory = surveyContext.getExpressionFactory();
			String parentExpr = def.getParentExpression();
			ModelPathExpression expression = expressionFactory.createModelPathExpression(parentExpr);
			Node<?> parentNode = expression.evaluate(context, null);
			if (parentNode != null && parentNode instanceof CodeAttribute) {
				return (CodeAttribute) parentNode;
			}
		} catch (Exception e) {
			// return null;
		}
		return null;
	}
	
	public List<ExternalCodeListItem> getChildItems(ExternalCodeListItem item) {
		return provider.getChildItems(item);
	}	
	
	public DatabaseExternalCodeListProvider getExternalCodeListProvider() {
		return provider;
	}
	
	public void setExternalCodeListProvider(
			DatabaseExternalCodeListProvider externalCodeListProvider) {
		this.provider = externalCodeListProvider;
	}

}
