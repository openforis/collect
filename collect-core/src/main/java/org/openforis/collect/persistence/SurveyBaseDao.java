package org.openforis.collect.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jooq.Record;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlValidator;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  @author S. Ricci
 */
abstract class SurveyBaseDao extends JooqDaoSupport {
	
	@Autowired
	protected CollectSurveyContext surveyContext;
	
	public void init() {
		
	}

	protected abstract <T extends CollectSurvey> T processSurveyRow(Record row);

	protected abstract SurveySummary processSurveySummaryRow(Record row);
	
	public <T extends CollectSurvey> T unmarshalIdml(String idml) throws IOException {
		byte[] bytes = idml.getBytes("UTF-8");
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		return unmarshalIdml(is);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CollectSurvey> T unmarshalIdml(InputStream is) throws IOException {
		SurveyIdmlBinder parser = new SurveyIdmlBinder(surveyContext);
		T survey;
		try {
			survey = (T) parser.unmarshal(is);
		} catch (XmlParseException e) {
			throw new DataInconsistencyException("Invalid idm", e);
		}
		return survey;
	}

	public void validateAgainstSchema(byte[] idml) throws InvalidIdmlException {
		IdmlValidator idmlValidator = new IdmlValidator();
		idmlValidator.validate(idml);
	}
	
	public String marshalSurvey(Survey survey) throws SurveyImportException {
		try {
			// Serialize Survey to XML
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			marshalSurvey(survey, os);
			return os.toString("UTF-8");
		} catch (IOException e) {
			throw new SurveyImportException("Error marshalling survey", e);
		}
	}
	
	public void marshalSurvey(Survey survey, OutputStream os) throws SurveyImportException {
//		try {
//			SurveyBinder parser = new SurveyBinder(surveyContext);
//			//parser.marshal(survey, os);
//		} catch (IOException e) {
//			throw new SurveyImportException("Error marshalling survey", e);
//		}
	}
	
}
