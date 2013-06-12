package org.openforis.collect.manager;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectCodeListPersisterContext;
import org.openforis.collect.persistence.CodeListItemDao;
import org.openforis.collect.persistence.DatabaseExternalCodeListProvider;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.metamodel.xml.SurveyCodeListPersisterBinder;
import org.openforis.idm.model.Code;
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
	@Autowired
	private CodeListItemDao codeListItemDao;
	@Autowired
	private CollectCodeListPersisterContext persisterContext;

	public CodeListItem loadItemByAttribute(CodeAttribute attribute) {
		CodeAttributeDefinition defn = attribute.getDefinition();
		CodeList list = defn.getList();
		if ( list.isExternal() ) {
			return provider.getItem(attribute);
		} else if ( list.isEmpty() ) {
			return loadPersistedCodeListItem(attribute);
		} else {
			return getInternalCodeListItem(attribute);
		}
	}

	protected CodeListItem getInternalCodeListItem(CodeAttribute attribute) {
		Code code = attribute.getValue();
		if (code != null) {
			String codeValue = code.getCode();
			if (StringUtils.isNotBlank(codeValue)) {
				ModelVersion currentVersion = attribute.getRecord().getVersion();
				CodeAttributeDefinition definition = attribute.getDefinition();
				String parentExpression = definition.getParentExpression();
				if (StringUtils.isBlank(parentExpression)) {
					return findCodeListItem(definition.getList().getItems(), codeValue, currentVersion);
				} else {
					CodeAttribute codeParent = attribute.getCodeParent();
					if (codeParent != null) {
						CodeListItem codeListItemParent = loadItemByAttribute(codeParent);
						if (codeListItemParent != null) {
							return findCodeListItem(codeListItemParent.getChildItems(), codeValue, currentVersion);
						}
					}
				}
			}
		}
		return null;
	}

	protected PersistedCodeListItem loadPersistedCodeListItem(CodeAttribute attribute) {
		Code code = attribute.getValue();
		if ( code == null || StringUtils.isBlank(code.getCode()) ) {
			return null;
		} else {
			PersistedCodeListItem parentItem = (PersistedCodeListItem) loadParentItem(attribute);
			if ( parentItem == null ) {
				return null;
			} else {
				CodeListItem item = loadPersistedItem(attribute, parentItem.getSystemId());
				return (PersistedCodeListItem) item;
			}
		}
	}
	
	protected CodeListItem loadParentItem(CodeAttribute attribute) {
		CodeList list = attribute.getDefinition().getList();
		if ( list.isExternal() ) {
			ExternalCodeListItem item = (ExternalCodeListItem) loadItemByAttribute(attribute);
			return provider.getParentItem(item);
		} else if ( list.isEmpty() ) {
			PersistedCodeListItem lastParentItem = null;
			List<CodeAttribute> codeAncestors = attribute.getCodeAncestors();
			for (int i = 0; i < codeAncestors.size(); i++) {
				CodeAttribute ancestor = codeAncestors.get(i);
				Integer lastParentItemId = lastParentItem == null ? null: lastParentItem.getSystemId();
				lastParentItem = (PersistedCodeListItem) loadPersistedItem(ancestor, lastParentItemId);
			}
			return lastParentItem;
		} else {
			CodeAttribute codeParent = attribute.getCodeParent();
			return loadItemByAttribute(codeParent);
		}
	}
	
	protected PersistedCodeListItem loadPersistedItem(CodeAttribute attribute, Integer parentItemId) {
		Code code = attribute.getValue();
		if ( code == null || StringUtils.isBlank(code.getCode()) ) {
			return null;
		} else {
			CodeAttributeDefinition defn = attribute.getDefinition();
			CodeList list = defn.getList();
			return codeListItemDao.loadItem(list, parentItemId, code.getCode());
		}
	}

	public ExternalCodeListItem loadExternalParentItem(ExternalCodeListItem item) {
		return provider.getParentItem(item);
	}

	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> List<T> loadRootItems(CodeList list) {
		if ( list.isExternal() ) {
			return (List<T>) provider.getRootItems(list);
		} else {
			return (List<T>) codeListItemDao.loadRootItems(list);
		}
	}

	public CodeListItem findCodeListItem(List<CodeListItem> siblings, String code, ModelVersion version) {
		String adaptedCode = code.trim();
		adaptedCode = adaptedCode.toUpperCase();
		//remove initial zeros
		adaptedCode = adaptedCode.replaceFirst("^0+", "");
		adaptedCode = Pattern.quote(adaptedCode);
		Pattern pattern = Pattern.compile("^[0]*" + adaptedCode + "$", Pattern.CASE_INSENSITIVE);

		for (CodeListItem item : siblings) {
			if ( version == null || version.isApplicable(item) ) {
				String itemCode = item.getCode();
				Matcher matcher = pattern.matcher(itemCode);
				if(matcher.find()) {
					return item;
				}
			}
		}
		return null;
	}

	public List<CodeListItem> getAssignableCodeListItems(Entity parent, CodeAttributeDefinition def) {
		List<? extends CodeListItem> items = null;
		CodeList list = def.getList();
		if ( StringUtils.isEmpty(def.getParentExpression()) ) {
			items = loadRootItems(list);
		} else {
			CodeAttribute parentCodeAttribute = getCodeParent(parent, def);
			if ( parentCodeAttribute != null ) {
				CodeListItem parentCodeListItem = loadItemByAttribute(parentCodeAttribute);
				if ( parentCodeListItem != null ) {
					items = loadChildItems(parentCodeListItem);
				}
			}
		}
		Record record = parent.getRecord();
		ModelVersion version = record.getVersion();
		return filterApplicableItems(items, version);
	}

	public void exportFromXMLAndStore(Survey survey, InputStream is) throws IdmlParseException {
		SurveyCodeListPersisterBinder binder = new SurveyCodeListPersisterBinder(persisterContext);
		binder.exportFromXMLAndStore(survey, is);
	}
	
	protected List<CodeListItem> filterApplicableItems(
			List<? extends CodeListItem> items, ModelVersion version) {
		if ( items == null ) {
			return Collections.emptyList();
		} else {
			List<? extends CodeListItem> result;
			if ( version == null ) {
				result = items;
			} else {
				result = version.filterApplicableItems(items);
			}
			return CollectionUtils.unmodifiableList(result);
		}
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
	
	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> List<T> loadChildItems(CodeListItem parent) {
		CodeList list = parent.getCodeList();
		if ( list.isExternal() ) {
			return (List<T>) provider.getChildItems((ExternalCodeListItem) parent);
		} else if ( list.isEmpty() ) {
			return (List<T>) codeListItemDao.loadItems(list, ((PersistedCodeListItem) parent).getSystemId());
		} else {
			return list.getItems();
		}
	}	
	
	public CodeListItem loadChildItem(CodeListItem parent, String code) {
		CodeList list = parent.getCodeList();
		if ( list.isExternal() ) {
			return provider.getChildItem((ExternalCodeListItem) parent, code);
		} else if ( list.isEmpty() ) {
			return codeListItemDao.loadItem(list, ((PersistedCodeListItem) parent).getSystemId(), code);
		} else {
			return parent.getChildItem(code);
		}
	}
	
	public void setExternalCodeListProvider(
			DatabaseExternalCodeListProvider externalCodeListProvider) {
		this.provider = externalCodeListProvider;
	}

}
