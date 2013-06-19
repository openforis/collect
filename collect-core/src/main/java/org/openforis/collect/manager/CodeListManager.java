package org.openforis.collect.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.exception.CodeListImportException;
import org.openforis.collect.model.CollectCodeListPersisterContext;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.CodeListItemDao;
import org.openforis.collect.persistence.DatabaseExternalCodeListProvider;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.PersistedCodeListItem;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@Transactional
public class CodeListManager {
	
	@Autowired
	private DatabaseExternalCodeListProvider provider;
	@Autowired
	private CodeListItemDao codeListItemDao;
	@Autowired
	private CollectCodeListPersisterContext persisterContext;

	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> T loadItemByAttribute(CodeAttribute attribute) {
		CodeAttributeDefinition defn = attribute.getDefinition();
		CodeList list = defn.getList();
		if ( list.isExternal() ) {
			return (T) provider.getItem(attribute);
		} else if ( list.isEmpty() ) {
			return (T) loadPersistedItem(attribute);
		} else {
			return (T) getInternalCodeListItem(attribute);
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

	protected PersistedCodeListItem loadPersistedItem(CodeAttribute attribute) {
		Code code = attribute.getValue();
		if ( code == null || StringUtils.isBlank(code.getCode()) ) {
			return null;
		} else {
			String codeVal = code.getCode();
			CodeAttributeDefinition defn = attribute.getDefinition();
			CodeList list = defn.getList();
			Record record = attribute.getRecord();
			ModelVersion version = record.getVersion();
			if ( StringUtils.isBlank(defn.getParentExpression()) ) {
				CodeListItem item = codeListItemDao.loadRootItem(list, codeVal, version);
				return (PersistedCodeListItem) item;
			} else {
				PersistedCodeListItem parentItem = (PersistedCodeListItem) loadParentItem(attribute);
				if ( parentItem == null ) {
					return null;
				} else {
					CodeListItem item = codeListItemDao.loadItem(list, parentItem.getSystemId(), codeVal, version);
					return (PersistedCodeListItem) item;
				}
			}
		}
	}
	
	protected CodeListItem loadParentItem(CodeAttribute attribute) {
		CodeList list = attribute.getDefinition().getList();
		Record record = attribute.getRecord();
		ModelVersion version = record.getVersion();
		if ( list.isExternal() ) {
			ExternalCodeListItem item = (ExternalCodeListItem) loadItemByAttribute(attribute);
			return provider.getParentItem(item);
		} else if ( list.isEmpty() ) {
			PersistedCodeListItem lastParentItem = null;
			List<CodeAttribute> codeAncestors = attribute.getCodeAncestors();
			for (int i = 0; i < codeAncestors.size(); i++) {
				CodeAttribute ancestor = codeAncestors.get(i);
				Integer lastParentItemId = lastParentItem == null ? null: lastParentItem.getSystemId();
				Code code = ancestor.getValue();
				lastParentItem = codeListItemDao.loadItem(list, lastParentItemId, code.getCode(), version);
				if ( lastParentItem == null ) {
					break;
				}
			}
			return lastParentItem;
		} else {
			CodeAttribute codeParent = attribute.getCodeParent();
			if ( codeParent == null ) {
				return null;
			} else {
				return loadItemByAttribute(codeParent);
			}
		}
	}
	
	public ExternalCodeListItem loadExternalParentItem(ExternalCodeListItem item) {
		return provider.getParentItem(item);
	}

	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> List<T> loadItems(CodeList list, int level) {
		if ( list.isExternal() ) {
			throw new UnsupportedOperationException();
		} else if ( list.isEmpty() ) {
			return (List<T>) codeListItemDao.loadItems(list, level);
		} else {
			return (List<T>) list.getItems(level - 1);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> List<T> loadRootItems(CodeList list) {
		if ( list.isExternal() ) {
			return (List<T>) provider.getRootItems(list);
		} else if ( list.isEmpty() ) {
			return (List<T>) codeListItemDao.loadRootItems(list);
		} else {
			return list.getItems();
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> T loadRootItem(CodeList list, String code, ModelVersion version) {
		if ( list.isExternal() ) {
			return (T) provider.getRootItem(list, code);
		} else if ( list.isEmpty() ) {
			return (T) codeListItemDao.loadRootItem(list, code, version);
		} else {
			return (T) list.getItem(code, 0, version);
		}
	}

	protected CodeListItem findCodeListItem(List<CodeListItem> siblings, String code, ModelVersion version) {
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
	
	public CodeListItem findAssignableItem(Entity parent,
			CodeAttributeDefinition defn, String code) {
		List<CodeListItem> items = findAssignableItems(parent, defn, code);
		return items.size() == 1 ? items.get(0): null;
	}

	public List<CodeListItem> findAssignableItems(Entity parent, CodeAttributeDefinition defn, String... codes) {
		List<CodeListItem> result = new ArrayList<CodeListItem>();
		List<CodeListItem> assignableItems = loadAssignableCodeListItems(parent, defn);
		if ( ! assignableItems.isEmpty() ) {
			Record record = parent.getRecord();
			ModelVersion version = record.getVersion();
			for (String code: codes) {
				CodeListItem item = findCodeListItem(assignableItems, code, version);
				if ( item != null ) {
					result.add(item);
				}
			}
		}
		return CollectionUtils.unmodifiableList(result);
	}

	public List<CodeListItem> loadAssignableCodeListItems(Entity parent, CodeAttributeDefinition def) {
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

	public void parseXMLAndStoreItems(CollectSurvey survey, InputStream is) throws CodeListImportException {
		SurveyCodeListPersisterBinder binder = new SurveyCodeListPersisterBinder(persisterContext);
		try {
			binder.exportFromXMLAndStore(survey, is);
		} catch (IdmlParseException e) {
			throw new CodeListImportException(e);
		}
	}
	
	public void parseXMLAndStoreItems(CollectSurvey survey, File file) throws CodeListImportException {
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			parseXMLAndStoreItems(survey, is);
		} catch (FileNotFoundException e) {
			throw new CodeListImportException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
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
			return (List<T>) codeListItemDao.loadChildItems((PersistedCodeListItem) parent);
		} else {
			return parent.getChildItems();
		}
	}
	
	public boolean hasChildItems(CodeListItem parent) {
		CodeList list = parent.getCodeList();
		if ( list.isExternal() ) {
			return provider.hasChildItems((ExternalCodeListItem) parent);
		} else if ( list.isEmpty() ) {
			return codeListItemDao.hasChildItems(list, ((PersistedCodeListItem) parent).getSystemId());
		} else {
			List<CodeListItem> childItems = parent.getChildItems();
			return ! childItems.isEmpty();
		}
	}
	
	public boolean hasQualifiableItems(CodeList list) {
		if ( list.isExternal() ) {
			return false;
		} else if ( list.isEmpty() ) {
			return codeListItemDao.hasQualifiableItems(list);
		} else {
			return list.isQualifiable();
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> T loadChildItem(T parent, String code, ModelVersion version) {
		CodeList list = parent.getCodeList();
		if ( list.isExternal() ) {
			return (T) provider.getChildItem((ExternalCodeListItem) parent, code);
		} else if ( list.isEmpty() ) {
			return (T) codeListItemDao.loadItem(list, ((PersistedCodeListItem) parent).getSystemId(), code, version);
		} else {
			return (T) parent.getChildItem(code);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CodeListItem> T loadParentItem(T item) {
		CodeList list = item.getCodeList();
		if ( list.isExternal() ) {
			return (T) provider.getParentItem((ExternalCodeListItem) item);
		} else if ( list.isEmpty() ) {
			return (T) codeListItemDao.loadById(list, ((PersistedCodeListItem) item).getParentId());
		} else {
			return (T) item.getParentItem();
		}
	}
	
	public void publishCodeLists(int publishedSurveyId, int surveyWorkId) {
		codeListItemDao.moveToPublishedSurvey(publishedSurveyId, surveyWorkId);
	}
	
	public void duplicateCodeListsForWork(CollectSurvey publishedSurvey, CollectSurvey workSurvey) {
		List<CodeList> codeLists = publishedSurvey.getCodeLists();
		for (CodeList codeList : codeLists) {
			if ( ! codeList.isExternal() ) {
				CodeList workList = workSurvey.getCodeListById(codeList.getId());
				List<PersistedCodeListItem> rootItems = loadRootItems(codeList);
				//duplicateFromPublishedToWorkSurvey(rootItems, workSurvey);
				duplicateItemsForWork(rootItems, workList, null);
			}
		}
	}
	
	protected PersistedCodeListItem duplicateForWork(PersistedCodeListItem item, CodeList workList, Integer parentItemId) {
		PersistedCodeListItem workItem = new PersistedCodeListItem(workList, item.getId());
		workItem.setCode(item.getCode());
		workItem.setParentId(parentItemId);
		List<LanguageSpecificText> labels = item.getLabels();
		for (LanguageSpecificText label : labels) {
			workItem.setLabel(label.getLanguage(), label.getText());
		}
		List<LanguageSpecificText> descriptions = item.getDescriptions();
		for (LanguageSpecificText description : descriptions) {
			workItem.setDescription(description.getLanguage(), description.getText());
		}
		codeListItemDao.insert(workItem);
		return workItem;
	}
	
	protected void duplicateItemsForWork(List<PersistedCodeListItem> items, CodeList workList, Integer parentWorkId) {
		for (PersistedCodeListItem item: items) {
			PersistedCodeListItem workItem = duplicateForWork(item, workList, parentWorkId);
			int newItemId = workItem.getId();
			List<PersistedCodeListItem> childItems = loadChildItems(item);
			duplicateItemsForWork(childItems, workList, newItemId);
		}
	}
	
	public void save(PersistedCodeListItem item) {
		if ( item.getSystemId() == null ) {
			codeListItemDao.insert(item);
		} else {
			codeListItemDao.update(item);
		}
	}
	
	public void delete(CodeListItem item) {
		CodeList list = item.getCodeList();
		if ( list.isExternal() ) {
			throw new UnsupportedOperationException();
		} else if ( list.isEmpty() ) {
			codeListItemDao.delete(((PersistedCodeListItem) item).getSystemId());
		} else {
			CodeListItem parentItem = item.getParentItem();
			int id = item.getId();
			if ( parentItem == null ) {
				CodeList codeList = item.getCodeList();
				codeList.removeItem(id);
			} else {
				parentItem.removeChildItem(id);
			}
		}
	}
	
	public void deleteAllItems(CodeList list) {
		if ( list.isExternal()) {
			throw new UnsupportedOperationException();
		} else if ( list.isEmpty() ) {
			codeListItemDao.deleteByCodeList(list);
		} else {
			list.removeAllItems();
		}
	}

	public void deleteBySurvey(Integer surveyId, boolean work) {
		codeListItemDao.deleteBySurvey(surveyId, work);
	}

	public void shiftItem(CodeListItem item, int indexTo) {
		CodeList list = item.getCodeList();
		if ( list.isExternal() ) {
			throw new UnsupportedOperationException();
		} else if ( list.isEmpty()) {
			codeListItemDao.shiftItem((PersistedCodeListItem) item, indexTo);
		} else {
			CodeListItem parentItem = item.getParentItem();
			if ( parentItem == null ) {
				CodeList codeList = item.getCodeList();
				codeList.moveItem(item, indexTo);
			} else {
				parentItem.moveChildItem(item, indexTo);
			}
		}
	}
	
	public void setExternalCodeListProvider(
			DatabaseExternalCodeListProvider externalCodeListProvider) {
		this.provider = externalCodeListProvider;
	}

}
