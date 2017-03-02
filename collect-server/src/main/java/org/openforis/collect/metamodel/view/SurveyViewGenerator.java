package org.openforis.collect.metamodel.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NodeLabel.Type;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyViewGenerator {

	private Locale locale;
	private boolean includeCodeLists = false;
	
	public SurveyViewGenerator(Locale locale) {
		super();
		this.locale = locale;
	}

	public List<SurveyView> generateViews(List<CollectSurvey> surveys) {
		List<SurveyView> result = new ArrayList<SurveyView>();
		for (CollectSurvey s : surveys) {
			result.add(generateView(s));
		}
		return result;
	}
	
	public SurveyView generateView(CollectSurvey survey) {
		final SurveyView surveyView = new SurveyView(survey);
		
		if (includeCodeLists) {
			List<CodeList> codeLists = survey.getCodeLists();
			for (CodeList codeList : codeLists) {
				CodeListView codeListView = new CodeListView();
				codeListView.setId(codeList.getId());
				codeListView.setName(codeList.getName());
				
				CodeListService service = survey.getContext().getCodeListService();
				List<CodeListItem> items = service.loadRootItems(codeList);
				for (CodeListItem item : items) {
					codeListView.items.add(createCodeListItemView(item));
				}
				
				surveyView.addCodeList(codeListView);
			}
		}
		
		final Map<Integer, NodeDefView> viewById = new HashMap<Integer, NodeDefView>();
		survey.getSchema().traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition def) {
				int id = def.getId();
				String name = def.getName();
				String label = getLabel(def);
				NodeDefView view;
				if (def instanceof EntityDefinition) {
					view = new EntityDefView(((EntityDefinition) def).isRoot(), id, name, label, def.isMultiple());
				} else {
					AttributeDefinition attrDef = (AttributeDefinition) def;
					view = new AttributeDefView(id, name, label, AttributeType.valueOf(attrDef), attrDef.getFieldNames(),
							attrDef.isKey(), attrDef.isMultiple());
				}
				NodeDefinition parentDef = def.getParentDefinition();
				if (parentDef == null) {
					surveyView.getSchema().addRootEntity((EntityDefView) view);
				} else {
					EntityDefView parentView = (EntityDefView) viewById.get(parentDef.getId());
					parentView.addChild(view);
				}
				viewById.put(id, view);
			}

		});
		return surveyView;
	}
	
	private CodeListItemView createCodeListItemView(CodeListItem item) {
		String langCode = locale.getLanguage();
		CodeListService service = item.getSurvey().getContext().getCodeListService();
		
		CodeListItemView itemView = new CodeListItemView();
		itemView.id = item.getId();
		itemView.code = item.getCode();
		itemView.label = item.getLabel(langCode);
		
		List<CodeListItemView> childItemsView = new ArrayList<CodeListItemView>();
		List<CodeListItem> childItems = service.loadChildItems(item);
		for (CodeListItem childItem : childItems) {
			childItemsView.add(createCodeListItemView(childItem));
		}
		itemView.items.addAll(childItemsView);
		return itemView;
	}

	private String getLabel(NodeDefinition def) {
		String langCode = locale.getLanguage();
		String label = def.getLabel(Type.INSTANCE, langCode);
		if (label == null && ! def.getSurvey().isDefaultLanguage(langCode)) {
			label = def.getLabel(Type.INSTANCE);
		}
		return label;
	}
	
	public void setIncludeCodeLists(boolean includeCodeLists) {
		this.includeCodeLists = includeCodeLists;
	}
	
}
