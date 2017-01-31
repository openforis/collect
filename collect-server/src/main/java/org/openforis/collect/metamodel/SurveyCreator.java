package org.openforis.collect.metamodel;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.ObjectUtils;
import org.openforis.collect.io.metadata.samplingpointdata.SamplingPointDataGenerator;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.SurveyObjectsGenerator;
import org.openforis.collect.metamodel.SimpleSurveyCreationParameters.ListItem;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.collect.persistence.xml.CeoApplicationOptions;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;

public class SurveyCreator {

	private SurveyManager surveyManager;
	private SamplingDesignManager samplingDesignManager;
	//TODO make it configurable
	private String languageCode = Locale.ENGLISH.getLanguage();
	private String singleAttributeSurveyCodeListName = "values";
	private String singleAttributeSurveyRootEntityName = "plot";
	private String singleAttributeSurveyIdAttributeName = "plot_id";
	private String singleAttributeSurveyAttributeName = "value";
	private String singleAttributeSurveyTabLabel = "Plot";
	
	public SurveyCreator(SurveyManager surveyManager, SamplingDesignManager samplingDesignManager) {
		super();
		this.surveyManager = surveyManager;
		this.samplingDesignManager = samplingDesignManager;
	}

	public CollectSurvey generateAndPublishSurvey(SimpleSurveyCreationParameters parameters)
			throws SurveyStoreException, SurveyImportException {
		CollectSurvey survey = createTemporarySingleAttributeSurvey(parameters.getName(), parameters.getValues());
		
		CeoApplicationOptions ceoApplicationOptions = new CeoApplicationOptions();
		ceoApplicationOptions.setSamplingPointDataConfiguration(parameters.getSamplingPointGenerationSettings());
		survey.addApplicationOptions(ceoApplicationOptions);
		
		surveyManager.save(survey);
		surveyManager.publish(survey);
		
		SamplingPointDataGenerator generator = new SamplingPointDataGenerator(
				survey,	parameters.getSamplingPointGenerationSettings());
		List<SamplingDesignItem> items = generator.generate();
		
		samplingDesignManager.insert(survey, items, true);
		return survey;
	}

	private CollectSurvey createTemporarySingleAttributeSurvey(String name, List<ListItem> list) {
		CollectSurvey survey = surveyManager.createTemporarySurvey(name, languageCode);

		CodeList codeList = survey.createCodeList();
		codeList.setName(singleAttributeSurveyCodeListName);
		for (int i = 0; i < list.size(); i++) {
			ListItem paramItem = list.get(i);
			CodeListItem item = codeList.createItem(1);
			item.setCode(ObjectUtils.defaultIfNull(paramItem.getCode(), String.valueOf(i + 1))); //specified code or item index
			item.setLabel(languageCode, paramItem.getLabel());
			codeList.addItem(item);
		}
		survey.addCodeList(codeList);
		
		Schema schema = survey.getSchema();

		EntityDefinition rootEntityDef = survey.getSchema().createEntityDefinition();
		rootEntityDef.setName(singleAttributeSurveyRootEntityName);
		schema.addRootEntityDefinition(rootEntityDef);
		
		CodeAttributeDefinition idAttrDef = schema.createCodeAttributeDefinition();
		idAttrDef.setName(singleAttributeSurveyIdAttributeName);
		idAttrDef.setKey(true);
		idAttrDef.setList(survey.getSamplingDesignCodeList());
		
		rootEntityDef.addChildDefinition(idAttrDef);
		
		CodeAttributeDefinition valueAttrDef = schema.createCodeAttributeDefinition();
		valueAttrDef.setName(singleAttributeSurveyAttributeName);
		valueAttrDef.setList(codeList);
		
		rootEntityDef.addChildDefinition(valueAttrDef);
		
		//create root tab set
		UIOptions uiOptions = survey.getUIOptions();
		UITabSet rootTabSet = uiOptions.createRootTabSet((EntityDefinition) rootEntityDef);
		UITab mainTab = uiOptions.getMainTab(rootTabSet);
		mainTab.setLabel(languageCode, singleAttributeSurveyTabLabel);
		
		SurveyObjectsGenerator surveyObjectsGenerator = new SurveyObjectsGenerator();
		surveyObjectsGenerator.addPredefinedObjects(survey);
		
		if ( survey.getSamplingDesignCodeList() == null ) {
			survey.addSamplingDesignCodeList();
		}
		return survey;
	}
}
