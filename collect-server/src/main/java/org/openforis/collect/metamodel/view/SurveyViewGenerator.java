package org.openforis.collect.metamodel.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.metamodel.ui.UIModelObject;
import org.openforis.collect.metamodel.uiconfiguration.view.Views;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserInGroup;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLabel;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.Unit;

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
		return generateView(survey, null, null);
	}

	public SurveyView generateView(final CollectSurvey survey, UserGroup userGroup,
			UserInGroup.UserGroupRole userInSurveyGroupRole) {
		final SurveyView surveyView = new SurveyView(survey, new ViewContext(languageCode));

		if (userGroup != null) {
			surveyView.setUserGroupQualifierName(userGroup.getQualifierName());
			surveyView.setUserGroupQualifierValue(userGroup.getQualifierValue());
			surveyView.setUserInGroupRole(userInSurveyGroupRole);
		}

		for (CodeList codeList : survey.getCodeLists()) {
			CodeListView codeListView = new CodeListView();
			codeListView.setId(codeList.getId());
			codeListView.setName(codeList.getName());
			codeListView.setLabel(codeList.getLabel(CodeListLabel.Type.ITEM, languageCode));

			if (includeCodeListValues && !codeList.isExternal()) {
				CodeListService service = survey.getContext().getCodeListService();
				List<CodeListItem> items = service.loadRootItems(codeList);
				for (CodeListItem item : items) {
					codeListView.addItem(createCodeListItemView(item));
				}
			}
			surveyView.addCodeList(codeListView);
		}

		for (ModelVersion version : survey.getVersions()) {
			ModelVersionView versionView = new ModelVersionView();
			versionView.setId(version.getId());
			versionView.setName(version.getName());
			versionView.setLabel(version.getLabel(languageCode));
			versionView.setDate(version.getDate());
			surveyView.addModelVersion(versionView);
		}
		
		for (Unit unit : survey.getUnits()) {
			UnitView unitView = new UnitView();
			unitView.setId(unit.getId());
			unitView.setConversionFactor(unit.getConversionFactor());
			unitView.setAbbreviation(unit.getAbbreviation(languageCode, survey.getDefaultLanguage()));
			unitView.setLabel(unit.getLabel(languageCode, survey.getDefaultLanguage()));
			surveyView.addUnit(unitView);
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
					boolean qualifier = survey.getAnnotations().isQualifier((AttributeDefinition) def);
					boolean showInSummary = survey.getAnnotations().isShowInSummary((AttributeDefinition) def);

					if (def instanceof CodeAttributeDefinition) {
						CodeAttributeDefinition attrDef = (CodeAttributeDefinition) def;
						int codeListId = attrDef.getList() == null ? -1 : attrDef.getList().getId();
						view = new CodeAttributeDefView(id, name, label, AttributeType.valueOf(attrDef),
								attrDef.getFieldNames(), attrDef.isKey(), attrDef.isMultiple(), showInSummary,
								qualifier, codeListId);
					} else if (def instanceof NumberAttributeDefinition) {
						NumberAttributeDefinition attrDef = (NumberAttributeDefinition) def;
						List<Precision> precisions = attrDef.getPrecisionDefinitions();
						List<PrecisionView> precisionViews = Views.fromObjects(precisions, PrecisionView.class);
						view = new NumberAttributeDefView(id, name, label, AttributeType.valueOf(attrDef),
								attrDef.getFieldNames(), attrDef.isKey(), attrDef.isMultiple(), showInSummary,
								qualifier);
						((NumericAttributeDefView) view).setNumericType(attrDef.getType());
						((NumericAttributeDefView) view).setPrecisions(precisionViews);
					} else {
						AttributeDefinition attrDef = (AttributeDefinition) def;
						view = new AttributeDefView(id, name, label, AttributeType.valueOf(attrDef),
								attrDef.getFieldNames(), attrDef.isKey(), attrDef.isMultiple(), showInSummary,
								qualifier);
					}
				}
				UIModelObject uiModelObject = survey.getUIConfiguration().getModelObjectByNodeDefinitionId(def.getId());
				view.setHideWhenNotRelevant(uiModelObject != null && uiModelObject.isHideWhenNotRelevant());

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
		itemView.color = item.getColor();

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
		if (label == null && !def.getSurvey().isDefaultLanguage(languageCode)) {
			label = def.getLabel(Type.INSTANCE);
		}
		return label;
	}

	public void setIncludeCodeListValues(boolean includeCodeListValues) {
		this.includeCodeListValues = includeCodeListValues;
	}

}
