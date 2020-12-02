package org.openforis.collect.metamodel.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.ui.UIConfiguration;
import org.openforis.collect.metamodel.ui.UIModelObject;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.uiconfiguration.view.Views;
import org.openforis.collect.metamodel.view.BooleanAttributeDefView.LayoutType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.utils.Dates;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.AttributeDefinition.FieldLabel;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
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
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
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

		final UIConfiguration uiConfiguration = survey.getUIConfiguration();

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
			versionView.setDate(Dates.formatDate(version.getDate()));
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
				String label = def.getLabel(Type.INSTANCE, languageCode);
				boolean multiple = def.isMultiple();
				NodeDefView view;
				if (def instanceof EntityDefinition) {
					EntityDefinition entityDefinition = (EntityDefinition) def;
					EntityDefView entityDefView = new EntityDefView(entityDefinition.isRoot(), id, name, label,
							multiple);
					entityDefView.setEnumerate(entityDefinition.isEnumerate() && entityDefinition.isEnumerable());
					view = entityDefView;
				} else {
					view = createAttributeDefView((AttributeDefinition) def);
				}
				view.setSinceVersionId(def.getSinceVersionId());
				view.setDeprecatedVersionId(def.getDeprecatedVersionId());
				
				UIModelObject uiModelObject = uiConfiguration.getModelObjectByNodeDefinitionId(def.getId());
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
		itemView.description = item.getDescription(languageCode);
		itemView.color = item.getColor();

		List<CodeListItemView> childItemsView = new ArrayList<CodeListItemView>();
		List<CodeListItem> childItems = service.loadChildItems(item);
		for (CodeListItem childItem : childItems) {
			childItemsView.add(createCodeListItemView(childItem));
		}
		itemView.items.addAll(childItemsView);
		return itemView;
	}

	private AttributeDefView createAttributeDefView(AttributeDefinition def) {
		CollectSurvey survey = def.getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();
		// TODO use UIConfiguration instead
		UIOptions uiOptions = survey.getUIOptions();

		int id = def.getId();
		String name = def.getName();
		String label = def.getLabel(Type.INSTANCE, languageCode);
		boolean multiple = def.isMultiple();
		AttributeDefView view;
		boolean qualifier = annotations.isQualifier(def);
		boolean showInSummary = annotations.isShowInSummary(def);
		AttributeType attributeType = AttributeType.valueOf(def);
		List<String> fieldNames = def.getFieldNames();
		boolean key = def.isKey();

		if (def instanceof BooleanAttributeDefinition) {
			BooleanAttributeDefinition booleanAttrDef = (BooleanAttributeDefinition) def;
			BooleanAttributeDefView attrDefView = new BooleanAttributeDefView(id, name, label, attributeType,
					fieldNames, key, multiple);
			attrDefView.setLayoutType(booleanAttrDef.isAffirmativeOnly() ? LayoutType.CHECKBOX : LayoutType.TEXTBOX);
			view = attrDefView;
		} else if (def instanceof CodeAttributeDefinition) {
			CodeAttributeDefinition codeAttrDef = (CodeAttributeDefinition) def;
			int codeListId = codeAttrDef.getList() == null ? -1 : codeAttrDef.getList().getId();
			CodeAttributeDefView attrDefView = new CodeAttributeDefView(id, name, label, attributeType, fieldNames, key,
					multiple);
			attrDefView.setCodeListId(codeListId);
			attrDefView.setEnumerator(codeAttrDef.isEnumerator());
			Integer codeParentDefId = codeAttrDef.getParentCodeAttributeDefinition() == null ? null
					: codeAttrDef.getParentCodeAttributeDefinition().getId();
			attrDefView.setParentCodeAttributeDefinitionId(codeParentDefId);
			attrDefView.setShowCode(uiOptions.getShowCode(codeAttrDef));
			attrDefView.setLayout(uiOptions.getLayoutType(codeAttrDef));
			attrDefView.setItemsOrientation(uiOptions.getLayoutDirection(codeAttrDef));
			view = attrDefView;
		} else if (def instanceof CoordinateAttributeDefinition) {
			CoordinateAttributeDefinition coordDef = (CoordinateAttributeDefinition) def;
			CoordinateAttributeDefView attrDefView = new CoordinateAttributeDefView(id, name, label, attributeType,
					fieldNames, key, multiple);
			attrDefView.setFieldsOrder(uiOptions.getFieldsOrder(coordDef));
			attrDefView.setShowSrsField(annotations.isShowSrsField(coordDef));
			attrDefView.setIncludeAccuracyField(annotations.isIncludeCoordinateAccuracy(coordDef));
			attrDefView.setIncludeAltitudeField(annotations.isIncludeCoordinateAltitude(coordDef));
			view = attrDefView;
		} else if (def instanceof FileAttributeDefinition) {
			FileAttributeDefinition fileDef = (FileAttributeDefinition) def;
			FileAttributeDefView attrDefView = new FileAttributeDefView(id, name, label, attributeType, fieldNames, key,
					multiple);
			attrDefView.setFileType(annotations.getFileType(fileDef));
			attrDefView.setMaxSize(fileDef.getMaxSize());
			attrDefView.setExtensions(fileDef.getExtensions());
			view = attrDefView;
		} else if (def instanceof NumericAttributeDefinition) {
			NumericAttributeDefinition numericDef = (NumericAttributeDefinition) def;
			NumericAttributeDefView attrDefView = def instanceof NumberAttributeDefinition
					? new NumberAttributeDefView(id, name, label, attributeType, fieldNames, key, multiple)
					: new RangeAttributeDefView(id, name, label, attributeType, fieldNames, key, multiple);
			attrDefView.setNumericType(numericDef.getType());
			List<Precision> precisions = numericDef.getPrecisionDefinitions();
			List<PrecisionView> precisionViews = Views.fromObjects(precisions, PrecisionView.class);
			attrDefView.setPrecisions(precisionViews);
			view = attrDefView;
		} else if (def instanceof TaxonAttributeDefinition) {
			TaxonAttributeDefinition taxonDef = (TaxonAttributeDefinition) def;
			TaxonAttributeDefView attrDefView = new TaxonAttributeDefView(id, name, label, attributeType, fieldNames,
					key, multiple);
			attrDefView.setTaxonomyName(taxonDef.getTaxonomy());
			attrDefView.setHighestRank(taxonDef.getHighestTaxonRank());
			attrDefView.setShowFamily(annotations.isShowFamily(taxonDef));
			attrDefView.setIncludeUniqueVernacularName(annotations.isIncludeUniqueVernacularName(taxonDef));
			attrDefView.setAllowUnlisted(annotations.isAllowUnlisted(taxonDef));
			view = attrDefView;
		} else if (def instanceof TextAttributeDefinition) {
			TextAttributeDefinition textDef = (TextAttributeDefinition) def;
			TextAttributeDefView attrDefView = new TextAttributeDefView(id, name, label, attributeType, fieldNames, key,
					multiple);
			attrDefView.setTextType(textDef.getType());
			view = attrDefView;
		} else {
			view = new AttributeDefView(id, name, label, attributeType, fieldNames, key, multiple);
		}
		view.setShowInRecordSummaryList(showInSummary);
		view.setQualifier(qualifier);
		List<FieldLabel> fieldLabels = def.getFieldLabels();
		Map<String, Boolean> visibilityByField = new HashMap<String, Boolean>();
		List<String> fieldLabelsView = new ArrayList<String>(fieldLabels.size());
		for (String fieldName : def.getFieldNames()) {
			fieldLabelsView.add(def.getFieldLabel(fieldName, languageCode));
			visibilityByField.put(fieldName, uiOptions.isVisibleField(def, fieldName));
		}
		view.setFieldLabels(fieldLabelsView);
		view.setVisibilityByField(visibilityByField);
		view.setCalculated(def.isCalculated());
		return view;
	}

	public void setIncludeCodeListValues(boolean includeCodeListValues) {
		this.includeCodeListValues = includeCodeListValues;
	}

}
