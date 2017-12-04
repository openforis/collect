package org.openforis.collect.metamodel;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.ObjectUtils;
import org.openforis.collect.io.metadata.samplingpointdata.SamplingPointDataGenerator;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.SurveyObjectsGenerator;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.metamodel.SimpleSurveyCreationParameters.ListItem;
import org.openforis.collect.metamodel.SimpleSurveyCreationParameters.SimpleCodeList;
import org.openforis.collect.metamodel.samplingdesign.SamplingPointGenerationSettings;
import org.openforis.collect.metamodel.ui.UIConfiguration;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptionsMigrator;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.collect.persistence.xml.CeoApplicationOptions;
import org.openforis.collect.utils.SurveyObjects;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLabel;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TextAttributeDefinition;

public class SurveyCreator {

	private SurveyManager surveyManager;
	private SamplingDesignManager samplingDesignManager;
	private UserGroupManager userGroupManager;
	//TODO make it configurable
	private String languageCode = Locale.ENGLISH.getLanguage();
	private String singleAttributeSurveyRootEntityName = "plot";
	private String singleAttributeSurveyKeyAttributeName = "plot_id";
	private String singleAttributeSurveyTabLabel = "Plot";
	private String singleAttributeSurveySecondLevelEntityName = "subplot";
	private String singleAttributeSurveySecondLevelIdAttributeName = "subplot_id";
	private String singleAttributeSurveyOperatorAttributeName = "operator";
	private String singleAttributeSurveyOperatorAttributeLabel = "Operator";
	
	public SurveyCreator(SurveyManager surveyManager, SamplingDesignManager samplingDesignManager,
			UserGroupManager userGroupManager) {
		super();
		this.surveyManager = surveyManager;
		this.samplingDesignManager = samplingDesignManager;
		this.userGroupManager = userGroupManager;
	}

	public CollectSurvey generateSimpleSurvey(SimpleSurveyCreationParameters parameters)
			throws SurveyStoreException, SurveyImportException {
		String projectName = parameters.getProjectName();
		String internalName = ObjectUtils.defaultIfNull(parameters.getName(), SurveyObjects.adjustInternalName(projectName));
		CollectSurvey existingSurvey = surveyManager.get(internalName);
		if (existingSurvey != null) {
			//TODO move it to validator
			throw new IllegalArgumentException(String.format("Survey with name %s already existing", internalName));
		}
		CollectSurvey survey = createTemporarySimpleSurvey(internalName, parameters.getCodeLists());
		survey.setProjectName(survey.getDefaultLanguage(), projectName);
		survey.setDescription(survey.getDefaultLanguage(), parameters.getDescription());
		UserGroup userGroup = userGroupManager.loadById(parameters.getUserGroupId());
		survey.setUserGroup(userGroup);
	
		CeoApplicationOptions ceoApplicationOptions = new CeoApplicationOptions();
		ceoApplicationOptions.setBaseMapSource(parameters.getCeoSettings().getBaseMapSource());
		ceoApplicationOptions.setImageryYear(parameters.getCeoSettings().getImageryYear());
		ceoApplicationOptions.setStackingProfile(parameters.getCeoSettings().getStackingProfile());
		SamplingPointGenerationSettings samplingPointGenerationSettings = parameters.getSamplingPointGenerationSettings();
		ceoApplicationOptions.setSamplingPointDataConfiguration(samplingPointGenerationSettings);
		survey.addApplicationOptions(ceoApplicationOptions);
		
		surveyManager.save(survey);
		
		SamplingPointDataGenerator generator = new SamplingPointDataGenerator(
				survey, parameters.getSamplingPointsByLevel(), samplingPointGenerationSettings);
		List<SamplingDesignItem> items = generator.generate();
		
		samplingDesignManager.insert(survey, items, true);
		return survey;
	}

	private CollectSurvey createTemporarySimpleSurvey(String name, List<SimpleCodeList> simpleCodeLists) {
		CollectSurvey survey = surveyManager.createTemporarySurvey(name, languageCode);

		for (int codeListIdx = 0; codeListIdx < simpleCodeLists.size(); codeListIdx++) {
			SimpleCodeList simpleCodeList = simpleCodeLists.get(codeListIdx);

			CodeList codeList = survey.createCodeList();
			codeList.setName("values_" + (codeListIdx+1));
			codeList.setLabel(CodeListLabel.Type.ITEM, survey.getDefaultLanguage(), simpleCodeList.getName());
			
			List<ListItem> items = simpleCodeList.getItems();
			for (int itemIdx = 0; itemIdx < items.size(); itemIdx++) {
				ListItem paramItem = items.get(itemIdx);
				CodeListItem item = codeList.createItem(1);
				item.setCode(ObjectUtils.defaultIfNull(paramItem.getCode(), String.valueOf(itemIdx + 1))); //specified code or item index
				item.setLabel(languageCode, paramItem.getLabel());
				item.setColor(paramItem.getColor());
				codeList.addItem(item);
			}
			survey.addCodeList(codeList);
		}
		
		Schema schema = survey.getSchema();

		EntityDefinition rootEntityDef = schema.createEntityDefinition();
		rootEntityDef.setName(singleAttributeSurveyRootEntityName);
		schema.addRootEntityDefinition(rootEntityDef);
		
		
		CodeAttributeDefinition idAttrDef = schema.createCodeAttributeDefinition();
		idAttrDef.setName(singleAttributeSurveyKeyAttributeName);
		idAttrDef.setKey(true);
		idAttrDef.setList(survey.getSamplingDesignCodeList());
		rootEntityDef.addChildDefinition(idAttrDef);
		
		TextAttributeDefinition operatorAttrDef = schema.createTextAttributeDefinition();
		operatorAttrDef.setName(singleAttributeSurveyOperatorAttributeName);
		operatorAttrDef.setKey(true);
		operatorAttrDef.setLabel(Type.INSTANCE, languageCode, singleAttributeSurveyOperatorAttributeLabel);
		survey.getAnnotations().setMeasurementAttribute(operatorAttrDef, true);
		rootEntityDef.addChildDefinition(operatorAttrDef);
		
		EntityDefinition secondLevelEntityDef = schema.createEntityDefinition();
		secondLevelEntityDef.setName(singleAttributeSurveySecondLevelEntityName);
		secondLevelEntityDef.setMultiple(true);
		rootEntityDef.addChildDefinition(secondLevelEntityDef);
		
		CodeAttributeDefinition secondLevelIdAttrDef = schema.createCodeAttributeDefinition();
		secondLevelIdAttrDef.setName(singleAttributeSurveySecondLevelIdAttributeName);
		secondLevelIdAttrDef.setKey(true);
		secondLevelIdAttrDef.setList(survey.getSamplingDesignCodeList());
		secondLevelIdAttrDef.setParentCodeAttributeDefinition(idAttrDef);
		secondLevelEntityDef.addChildDefinition(secondLevelIdAttrDef);
		
		for (int i = 0; i < simpleCodeLists.size(); i++) {
			String codeListName = "values_" + (i+1);
			CodeList codeList = survey.getCodeList(codeListName);
			CodeAttributeDefinition valueAttrDef = schema.createCodeAttributeDefinition();
			valueAttrDef.setName(codeListName);
			valueAttrDef.setList(codeList);
			secondLevelEntityDef.addChildDefinition(valueAttrDef);
		}
		
		//create root tab set
		UIOptions uiOptions = survey.getUIOptions();
		UITabSet rootTabSet = uiOptions.createRootTabSet((EntityDefinition) rootEntityDef);
		UITab mainTab = uiOptions.getMainTab(rootTabSet);
		mainTab.setLabel(languageCode, singleAttributeSurveyTabLabel);
		
		UIConfiguration uiConfiguration = new UIOptionsMigrator().migrateToUIConfiguration(uiOptions);
		survey.setUIConfiguration(uiConfiguration);

		SurveyObjectsGenerator surveyObjectsGenerator = new SurveyObjectsGenerator();
		surveyObjectsGenerator.addPredefinedObjects(survey);
		
		if ( survey.getSamplingDesignCodeList() == null ) {
			survey.addSamplingDesignCodeList();
		}
		return survey;
	}
}
