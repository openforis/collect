package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcRecord.OFC_RECORD;
import static org.openforis.collect.persistence.jooq.tables.OfcSurvey.OFC_SURVEY;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.impl.Factory;
import org.jooq.impl.SQLDataType;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
@Transactional
public class SurveyDao extends JooqDaoSupport {
	// private final Log LOG = LogFactory.getLog(SurveyDao.class);

	private CollectIdmlBindingContext bindingContext;

	@Autowired
	private ExpressionFactory expressionFactory;
	@Autowired
	private Validator validator;
	@Autowired
	private ExternalCodeListProvider externalCodeListProvider;

	public SurveyDao() {
	}

	public void init() {
		bindingContext = new CollectIdmlBindingContext(
				new CollectSurveyContext(expressionFactory, validator,
						externalCodeListProvider));
	}

	@Transactional
	public void importModel(Survey survey) throws SurveyImportException {
		String name = survey.getName();
		if (StringUtils.isBlank(name)) {
			throw new SurveyImportException(
					"Survey name must be set before importing");
		}

		String idml = marshalSurvey(survey);

		// Insert into OFC_SURVEY table
		Factory jf = getJooqFactory();
		int surveyId = jf.nextval(OFC_SURVEY_ID_SEQ).intValue();
		jf.insertInto(OFC_SURVEY).set(OFC_SURVEY.ID, surveyId)				
				.set(OFC_SURVEY.NAME, survey.getName())
				.set(OFC_SURVEY.URI, survey.getUri())
				.set(OFC_SURVEY.IDML, Factory.val(idml, SQLDataType.CLOB))
				.execute();

		survey.setId(surveyId);

		//insertNodeDefinitions(survey);
	}

	public Survey load(int id) {
		Factory jf = getJooqFactory();
		Record record = jf.select().from(OFC_SURVEY)
				.where(OFC_SURVEY.ID.equal(id)).fetchOne();
		Survey survey = processSurveyRow(record);
		if (survey != null) {
			//loadNodeDefinitions(survey);
		}
		return survey;
	}

	public CollectSurvey load(String name) {
		Factory jf = getJooqFactory();
		Record record = jf.select().from(OFC_SURVEY)
				.where(OFC_SURVEY.NAME.equal(name)).fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		if (survey != null) {
			//loadNodeDefinitions(survey);
		}
		return survey;
	}

	@Transactional
	public List<CollectSurvey> loadAll() {
		Factory jf = getJooqFactory();
		List<CollectSurvey> surveys = new ArrayList<CollectSurvey>();
		Result<Record> results = jf.select().from(OFC_SURVEY).fetch();
		for (Record row : results) {
			CollectSurvey survey = processSurveyRow(row);
			if (survey != null) {
				//loadNodeDefinitions(survey);
				surveys.add(survey);
			}
		}
		return surveys;
	}

	private CollectSurvey processSurveyRow(Record row) {
		try {
			if (row == null) {
				return null;
			}
			String idml = row.getValueAsString(OFC_SURVEY.IDML);
			CollectSurvey survey = (CollectSurvey) unmarshalIdml(idml);
			survey.setId(row.getValueAsInteger(OFC_SURVEY.ID));
			survey.setName(row.getValue(OFC_SURVEY.NAME));
			return survey;
		} catch (IOException e) {
			throw new RuntimeException(
					"Error deserializing IDML from database", e);
		}
	}

	private Survey unmarshalIdml(String idml) throws IOException {
		byte[] bytes = idml.getBytes("UTF-8");
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		SurveyUnmarshaller su = bindingContext.createSurveyUnmarshaller();
		CollectSurvey survey;
		try {
			survey = (CollectSurvey) su.unmarshal(is);
		} catch (InvalidIdmlException e) {
			throw new DataInconsistencyException("Invalid idm");
		}
		return survey;
	}

	private String marshalSurvey(Survey survey) throws SurveyImportException {
		try {
			// Serialize Survey to XML
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			SurveyMarshaller sm = bindingContext.createSurveyMarshaller();
			sm.setIndent(true);
			sm.marshal(survey, os);
			return os.toString("UTF-8");
		} catch (IOException e) {
			throw new SurveyImportException("Error unmarshalling survey", e);
		}
	}

	public void clearModel() {
		Factory jf = getJooqFactory();
		jf.delete(OFC_RECORD).execute();
		jf.delete(OFC_SURVEY).execute();
	}

	public CollectIdmlBindingContext getBindingContext() {
		return bindingContext;
	}

	public void updateModel(CollectSurvey survey) throws SurveyImportException {
		String name = survey.getName();
		if (StringUtils.isBlank(name)) {
			throw new SurveyImportException(
					"Survey name must be set before importing");
		}

		String idml = marshalSurvey(survey);

		// Get OFC_SURVEY table id for name
		Factory jf = getJooqFactory();
		int surveyId = 0;
		SelectConditionStep query = jf.select(OFC_SURVEY.ID).from(OFC_SURVEY)
				.where(OFC_SURVEY.NAME.equal(name));
		query.execute();
		Result<Record> result = query.getResult();

		System.out.println("Checking survey");
		if (result.isEmpty()) { // we should insert it now			
			surveyId = jf.nextval(OFC_SURVEY_ID_SEQ).intValue();
			System.out.println("    Survey " +  name + " not exist. Inserting with ID = " + surveyId );
			jf.insertInto(OFC_SURVEY).set(OFC_SURVEY.ID, surveyId)
					.set(OFC_SURVEY.NAME, survey.getName())
					.set(OFC_SURVEY.URI, survey.getUri())
					.set(OFC_SURVEY.IDML, Factory.val(idml, SQLDataType.CLOB))
					.execute();
			survey.setId(surveyId);
		} else {
			Record record = result.get(0);			
			surveyId = record.getValueAsInteger(OFC_SURVEY.ID);			
			survey.setId(surveyId);
			System.out.println("    Survey " +  name + " exist. Updating with ID = " + surveyId );
			jf.update(OFC_SURVEY)
					.set(OFC_SURVEY.IDML, Factory.val(idml, SQLDataType.CLOB))
					.set(OFC_SURVEY.NAME, survey.getName())
					.set(OFC_SURVEY.URI, survey.getUri())
					.where(OFC_SURVEY.ID.equal(survey.getId())).execute();
		}

	}
}
