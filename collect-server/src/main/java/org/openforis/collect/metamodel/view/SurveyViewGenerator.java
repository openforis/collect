package org.openforis.collect.metamodel.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.ui.UIModelObject;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.uiconfiguration.view.Views;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserInGroup;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.AttributeDefinition.FieldLabel;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLabel;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
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
		String defaultLanguage = survey.getDefaultLanguage();
		CollectAnnotations annotations = survey.getAnnotations();
		// TODO use UIConfiguration instead
		UIOptions uiOptions = survey.getUIOptions();

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
			unitView.setAbbreviation(unit.getAbbreviation(languageCode, defaultLanguage));
			unitView.setLabel(unit.getLabel(languageCode, defaultLanguage));
			surveyView.addUnit(unitView);
		}

		for (SpatialReferenceSystem srs : survey.getSpatialReferenceSystems()) {
			SpatialReferenceSystemView srsView = new SpatialReferenceSystemView();
			srsView.setId(srs.getId());
			srsView.setLabel(srs.getLabel(languageCode, defaultLanguage));
			srsView.setDescription(srs.getDescription(languageCode, defaultLanguage));
			surveyView.addSpatialReferenceSystem(srsView);
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
					boolean qualifier = annotations.isQualifier((AttributeDefinition) def);
					boolean showInSummary = annotations.isShowInSummary((AttributeDefinition) def);

					if (def instanceof CodeAttributeDefinition) {
						CodeAttributeDefinition attrDef = (CodeAttributeDefinition) def;
						int codeListId = attrDef.getList() == null ? -1 : attrDef.getList().getId();
						view = new CodeAttributeDefView(id, name, label, AttributeType.valueOf(attrDef),
								attrDef.getFieldNames(), attrDef.isKey(), attrDef.isMultiple(), showInSummary,
								qualifier, codeListId);
					} else if (def instanceof CoordinateAttributeDefinition) {
						CoordinateAttributeDefinition attrDef = (CoordinateAttributeDefinition) def;
						CoordinateAttributeDefView attrDefView = new CoordinateAttributeDefView(id, name, label,
								AttributeType.valueOf(attrDef), attrDef.getFieldNames(), attrDef.isKey(),
								attrDef.isMultiple(), showInSummary, qualifier);
						attrDefView.setFieldsOrder(uiOptions.getFieldsOrder(attrDef));
						attrDefView.setShowSrsField(annotations.isShowSrsField(attrDef));
						attrDefView.setIncludeAccuracyField(annotations.isIncludeCoordinateAccuracy(attrDef));
						attrDefView.setIncludeAltitudeField(annotations.isIncludeCoordinateAltitude(attrDef));
						view = attrDefView;
					} else if (def instanceof FileAttributeDefinition) {
						FileAttributeDefinition attrDef = (FileAttributeDefinition) def;
						FileAttributeDefView attrDefView = new FileAttributeDefView(id, name, label,
								AttributeType.valueOf(attrDef), attrDef.getFieldNames(), attrDef.isKey(),
								attrDef.isMultiple(), showInSummary, qualifier);
						attrDefView.setFileType(annotations.getFileType(attrDef));
						attrDefView.setMaxSize(attrDef.getMaxSize());
						attrDefView.setExtensions(attrDef.getExtensions());
						view = attrDefView;
					} else if (def instanceof NumberAttributeDefinition) {
						NumberAttributeDefinition attrDef = (NumberAttributeDefinition) def;
						List<Precision> precisions = attrDef.getPrecisionDefinitions();
						List<PrecisionView> precisionViews = Views.fromObjects(precisions, PrecisionView.class);
						NumericAttributeDefView attrDefView = new NumberAttributeDefView(id, name, label,
								AttributeType.valueOf(attrDef), attrDef.getFieldNames(), attrDef.isKey(),
								attrDef.isMultiple(), showInSummary, qualifier);
						attrDefView.setNumericType(attrDef.getType());
						attrDefView.setPrecisions(precisionViews);
						view = attrDefView;
					} else if (def instanceof TaxonAttributeDefinition) {
						TaxonAttributeDefinition attrDef = (TaxonAttributeDefinition) def;
						TaxonAttributeDefView attrDefView = new TaxonAttributeDefView(id, name, label,
								AttributeType.valueOf(attrDef), attrDef.getFieldNames(), attrDef.isKey(),
								attrDef.isMultiple(), showInSummary, qualifier);
						attrDefView.setTaxonomyName(attrDef.getTaxonomy());
						attrDefView.setHighestRank(attrDef.getHighestTaxonRank());
						attrDefView.setCodeVisible(
								uiOptions.isVisibleField(attrDef, TaxonAttributeDefinition.CODE_FIELD_NAME));
						attrDefView.setScientificNameVisible(
								uiOptions.isVisibleField(attrDef, TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME));
						attrDefView.setVernacularNameVisible(
								uiOptions.isVisibleField(attrDef, TaxonAttributeDefinition.VERNACULAR_NAME_FIELD_NAME));
						attrDefView.setLanguageCodeVisible(
								uiOptions.isVisibleField(attrDef, TaxonAttributeDefinition.LANGUAGE_CODE_FIELD_NAME));
						attrDefView.setLanguageVarietyVisible(uiOptions.isVisibleField(attrDef,
								TaxonAttributeDefinition.LANGUAGE_VARIETY_FIELD_NAME));
						attrDefView.setShowFamily(annotations.isShowFamily(attrDef));
						attrDefView.setIncludeUniqueVernacularName(annotations.isIncludeUniqueVernacularName(attrDef));
						attrDefView.setAllowUnlisted(annotations.isAllowUnlisted(attrDef));
						view = attrDefView;
					} else {
						AttributeDefinition attrDef = (AttributeDefinition) def;
						view = new AttributeDefView(id, name, label, AttributeType.valueOf(attrDef),
								attrDef.getFieldNames(), attrDef.isKey(), attrDef.isMultiple(), showInSummary,
								qualifier);
					}
					AttributeDefinition attrDef = ((AttributeDefinition) def);
					List<FieldLabel> fieldLabels = attrDef.getFieldLabels();
					List<String> fieldLabelsView = new ArrayList<String>(fieldLabels.size());
					for (String fieldName : ((AttributeDefinition) def).getFieldNames()) {
						fieldLabelsView.add(attrDef.getFieldLabel(fieldName, languageCode));
					}
					((AttributeDefView) view).setFieldLabels(fieldLabelsView);
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
