package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.tables.OfcSurveyWork.OFC_SURVEY_WORK;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jooq.Record;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyMarshaller;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  @author S. Ricci
 */
class SurveyBaseDao extends JooqDaoSupport {
	
	protected CollectIdmlBindingContext bindingContext;

	protected CollectSurveyContext surveyContext;
	
	@Autowired
	protected ExpressionFactory expressionFactory;
	@Autowired
	protected Validator validator;
	@Autowired
	protected ExternalCodeListProvider externalCodeListProvider;

	public void init() {
		surveyContext = new CollectSurveyContext(expressionFactory, validator,
				externalCodeListProvider);
		bindingContext = new CollectIdmlBindingContext(
				surveyContext);
	}
	
	protected <T extends CollectSurvey> T processSurveyRow(Record row) {
		try {
			if (row == null) {
				return null;
			}
			String idml = row.getValueAsString(OFC_SURVEY_WORK.IDML);
			T survey = unmarshalIdml(idml);
			survey.setId(row.getValueAsInteger(OFC_SURVEY_WORK.ID));
			survey.setName(row.getValue(OFC_SURVEY_WORK.NAME));
			return survey;
		} catch (IOException e) {
			throw new RuntimeException(
					"Error deserializing IDML from database", e);
		}
	}

	public <T extends CollectSurvey> T unmarshalIdml(String idml) throws IOException {
		byte[] bytes = idml.getBytes("UTF-8");
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		return unmarshalIdml(is);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CollectSurvey> T unmarshalIdml(InputStream is) throws IOException {
		SurveyUnmarshaller su = bindingContext.createSurveyUnmarshaller();
		T survey;
		try {
			survey = (T) su.unmarshal(is);
		} catch (InvalidIdmlException e) {
			throw new DataInconsistencyException("Invalid idm");
		}
		return survey;
	}

	public void validateAgainstSchema(byte[] idml) throws InvalidIdmlException {
		SurveyUnmarshaller su = bindingContext.createSurveyUnmarshaller();
		su.validateAgainstSchema(idml);
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
		try {
			SurveyMarshaller sm = bindingContext.createSurveyMarshaller();
			sm.setIndent(true);
			sm.marshal(survey, os);
		} catch (IOException e) {
			throw new SurveyImportException("Error marshalling survey", e);
		}
	}
	
	public CollectIdmlBindingContext getBindingContext() {
		return bindingContext;
	}
	
}
