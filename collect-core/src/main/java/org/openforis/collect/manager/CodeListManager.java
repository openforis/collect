package org.openforis.collect.manager;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectCodeListPersisterContext;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.CodeListItemDao;
import org.openforis.collect.persistence.DatabaseExternalCodeListProvider;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.ModelVersion;
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

	public ExternalCodeListItem loadCodeListItem(CodeAttribute attribute) {
		CodeAttributeDefinition defn = attribute.getDefinition();
		CodeList list = defn.getList();
		if ( list.isExternal() ) {
			return provider.getItem(attribute);
		} else {
			return loadInternalCodeListItem(attribute);
		}
	}

	protected ExternalCodeListItem loadInternalCodeListItem(CodeAttribute attribute) {
		Code code = attribute.getValue();
		if ( code == null || StringUtils.isBlank(code.getCode()) ) {
			return null;
		} else {
			ExternalCodeListItem parentItem = loadParentItem(attribute);
			if ( parentItem == null ) {
				return null;
			} else {
				ExternalCodeListItem item = loadItem(attribute, parentItem.getSystemId());
				return item;
			}
		}
	}
	
	protected ExternalCodeListItem loadParentItem(CodeAttribute attribute) {
		ExternalCodeListItem lastParentItem = null;
		List<CodeAttribute> codeAncestors = attribute.getCodeAncestors();
		for (int i = 0; i < codeAncestors.size(); i++) {
			CodeAttribute ancestor = codeAncestors.get(i);
			Integer lastParentItemId = lastParentItem == null ? null: lastParentItem.getSystemId();
			lastParentItem = loadItem(ancestor, lastParentItemId);
		}
		return lastParentItem;
	}
	
	protected ExternalCodeListItem loadItem(CodeAttribute attribute, Integer parentItemId) {
		Code code = attribute.getValue();
		if ( code == null || StringUtils.isBlank(code.getCode()) ) {
			return null;
		} else {
			CollectSurvey survey = (CollectSurvey) attribute.getSurvey();
			CodeAttributeDefinition defn = attribute.getDefinition();
			CodeList list = defn.getList();
			ExternalCodeListItem item = codeListItemDao.loadItem(survey.getId(), 
					survey.isWork(), list.getId(), parentItemId, code.getCode());
			return item;
		}
	}

	public ExternalCodeListItem loadExternalParentItem(ExternalCodeListItem item) {
		return provider.getParentItem(item);
	}

	public List<ExternalCodeListItem> loadRootItems(CodeList list) {
		if ( list.isExternal() ) {
			return provider.getRootItems(list);
		} else {
			CollectSurvey survey = (CollectSurvey) list.getSurvey();
			return codeListItemDao.loadRootItems(survey.getId(), survey.isWork(), list.getId());
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
		List<? extends CodeListItem> items = null;
		CodeList list = def.getList();
		if ( StringUtils.isEmpty(def.getParentExpression()) ) {
			items = loadRootItems(list);
		} else {
			CodeAttribute parentCodeAttribute = getCodeParent(parent, def);
			if ( parentCodeAttribute != null ) {
				ExternalCodeListItem parentCodeListItem = loadCodeListItem(parentCodeAttribute);
				if ( parentCodeListItem != null ) {
					items = loadChildItems(parentCodeListItem);
				}
			}
		}
		Record record = parent.getRecord();
		ModelVersion version = record.getVersion();
		return filterApplicableItems(items, version);
	}

	public void exportFromXMLAndStore(InputStream is) throws IdmlParseException {
		SurveyCodeListPersisterBinder binder = new SurveyCodeListPersisterBinder(persisterContext);
		binder.exportFromXMLAndStore(is);
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
	
	public List<ExternalCodeListItem> loadChildItems(ExternalCodeListItem item) {
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
