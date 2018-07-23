package org.openforis.collect.persistence.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.ui.UIConfiguration;
import org.openforis.collect.metamodel.ui.UIOptionsMigrator;
import org.openforis.collect.metamodel.ui.UIOptionsMigrator.UIOptionsMigrationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectSurveyIdmlBinder extends SurveyIdmlBinder {

	private final Logger log = LogManager.getLogger(CollectSurveyIdmlBinder.class);
	
	public CollectSurveyIdmlBinder(SurveyContext surveyContext) {
		super(surveyContext);
		addApplicationOptionsBinder(new UIOptionsBinder());
		addApplicationOptionsBinder(new CeoApplicationOptionsBinder());
	}
	
	public String marshal(Survey survey) throws SurveyImportException {
		try {
			// Serialize Survey to XML
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			marshal(survey, os);
			return new String(os.toByteArray(), OpenForisIOUtils.UTF_8);
		} catch (IOException e) {
			throw new SurveyImportException("Error marshalling survey", e);
		}
	}
	
	@Override
	protected void onUnmarshallingComplete(Survey survey) {
		super.onUnmarshallingComplete(survey);
		CollectSurvey collectSurvey = (CollectSurvey) survey;
		CollectAnnotations annotations = collectSurvey.getAnnotations();
		collectSurvey.setTarget(annotations.getSurveyTarget());
		collectSurvey.setCollectVersion(annotations.getCollectVersion());
		
		if (collectSurvey.getUIOptions() != null) {
			try {
				UIConfiguration uiConfiguration = new UIOptionsMigrator().migrateToUIConfiguration(collectSurvey.getUIOptions());
				collectSurvey.setUIConfiguration(uiConfiguration);
			} catch(UIOptionsMigrationException e) {
				log.error("Error generating UI model for survey " + collectSurvey.getUri() + ": " + e.getMessage());
			} catch(Exception e) {
				log.error("Error generating UI model for survey " + collectSurvey.getUri() + ": " + e.getMessage(), e);
			}
		}
	}
	
}
