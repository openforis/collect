package org.openforis.collect.persistence.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

	public CollectSurveyIdmlBinder(SurveyContext surveyContext) {
		super(surveyContext);
		addApplicationOptionsBinder(new UIOptionsBinder());
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
	
}
