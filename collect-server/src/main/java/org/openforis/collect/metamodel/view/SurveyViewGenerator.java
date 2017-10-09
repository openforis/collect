package org.openforis.collect.metamodel.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
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

	private boolean includeCodeListValues = false;
	private String languageCode;
	
	public SurveyViewGenerator(String languageCode) {
		super();
		this.languageCode = languageCode;
	}

	public List<SurveyView> generateViews(List<CollectSurvey> surveys) {
		List<SurveyView> result = new ArrayList<SurveyView>();
		for (CollectSurvey s : surveys) {
			result.add(generateView(s));
		}
		return result;
	}
	
	public SurveyView generateView(final CollectSurvey survey) {
		final SurveyView surveyView = new SurveyView(survey);

		List<CodeList> codeLists = survey.getCodeLists();
		for (CodeList codeList : codeLists) {
			CodeListView codeListView = new CodeListView();
			codeListView.setId(codeList.getId());
			codeListView.setName(codeList.getName());

			if (includeCodeListValues && ! codeList.isExternal()) {
				CodeListService service = survey.getContext().getCodeListService();
				List<CodeListItem> items = service.loadRootItems(codeList);
				for (CodeListItem item : items) {
					codeListView.items.add(createCodeListItemView(item));
				}
			}
			surveyView.addCodeList(codeListView);
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
				} else if (def instanceof CodeAttributeDefinition) {
					CodeAttributeDefinition attrDef = (CodeAttributeDefinition) def;
					int codeListId = attrDef.getList() == null ? -1: attrDef.getList().getId();
					view = new CodeAttributeDefView(id, name, label, AttributeType.valueOf(attrDef), attrDef.getFieldNames(), 
							attrDef.isKey(), attrDef.isMultiple(), survey.getAnnotations().isShowInSummary(attrDef), codeListId);
				} else {
					AttributeDefinition attrDef = (AttributeDefinition) def;
					view = new AttributeDefView(id, name, label, AttributeType.valueOf(attrDef), attrDef.getFieldNames(),
							attrDef.isKey(), attrDef.isMultiple(), survey.getAnnotations().isShowInSummary(attrDef));
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
		CodeListService service = item.getSurvey().getContext().getCodeListService();
		
		CodeListItemView itemView = new CodeListItemView();
		itemView.id = item.getId();
		itemView.code = item.getCode();
		itemView.label = item.getLabel(languageCode);
		
		List<CodeListItemView> childItemsView = new ArrayList<CodeListItemView>();
		List<CodeListItem> childItems = service.loadChildItems(item);
		for (CodeListItem childItem : childItems) {
			childItemsView.add(createCodeListItemView(childItem));
		}
		itemView.items.addAll(childItemsView);
		return itemView;
	}

	private String getLabel(NodeDefinition def) {
		String label = def.getLabel(Type.INSTANCE, languageCode);
		if (label == null && ! def.getSurvey().isDefaultLanguage(languageCode)) {
			label = def.getLabel(Type.INSTANCE);
		}
		return label;
	}
	
	public void setIncludeCodeListValues(boolean includeCodeListValues) {
		this.includeCodeListValues = includeCodeListValues;
	}
	
}
