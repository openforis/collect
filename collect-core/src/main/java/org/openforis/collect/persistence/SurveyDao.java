package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcRecord.OFC_RECORD;
import static org.openforis.collect.persistence.jooq.tables.OfcSurvey.OFC_SURVEY;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.impl.Factory;
import org.jooq.impl.SQLDataType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
@Transactional
public class SurveyDao extends SurveyBaseDao {
	private final Log LOG = LogFactory.getLog(SurveyDao.class);

	@Transactional
	public void importModel(Survey survey) throws SurveyImportException {
		String name = survey.getName();
		if (StringUtils.isBlank(name)) {
			throw new SurveyImportException(
					"Survey name must be set before importing");
		}

		String idml = marshalSurvey(survey);

		// Insert into OFC_SURVEY table
		DialectAwareJooqFactory jf = getJooqFactory();
		int surveyId = jf.nextId(OFC_SURVEY.ID, OFC_SURVEY_ID_SEQ);
		jf.insertInto(OFC_SURVEY).set(OFC_SURVEY.ID, surveyId)				
				.set(OFC_SURVEY.NAME, survey.getName())
				.set(OFC_SURVEY.URI, survey.getUri())
				.set(OFC_SURVEY.IDML, Factory.val(idml, SQLDataType.CLOB))
				.execute();

		survey.setId(surveyId);
	}

	public Survey load(int id) {
		Factory jf = getJooqFactory();
		Record record = jf.select().from(OFC_SURVEY)
				.where(OFC_SURVEY.ID.equal(id)).fetchOne();
		Survey survey = processSurveyRow(record);
		return survey;
	}

	public CollectSurvey load(String name) {
		Factory jf = getJooqFactory();
		Record record = jf.select().from(OFC_SURVEY)
				.where(OFC_SURVEY.NAME.equal(name)).fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}

	public CollectSurvey loadByUri(String uri) {
		Factory jf = getJooqFactory();
		Record record = jf.select().from(OFC_SURVEY)
				.where(OFC_SURVEY.URI.equal(uri)).fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}
	
	public SurveySummary loadSurveySummary(int id) {
		Factory jf = getJooqFactory();
		Record record = jf.select()
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.ID.equal(id))
				.fetchOne();
		SurveySummary result = processSurveySummaryRow(record);
		return result;
	}
	
	public SurveySummary loadSurveySummaryByName(String name) {
		Factory jf = getJooqFactory();
		Record record = jf.select()
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.NAME.equal(name))
				.fetchOne();
		SurveySummary result = processSurveySummaryRow(record);
		return result;
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

	public void clearModel() {
		Factory jf = getJooqFactory();
		jf.delete(OFC_RECORD).execute();
		jf.delete(OFC_SURVEY).execute();
	}

	public void updateModel(CollectSurvey survey) throws SurveyImportException {
		String name = survey.getName();
		if (StringUtils.isBlank(name)) {
			throw new SurveyImportException(
					"Survey name must be set before importing");
		}

		String idml = marshalSurvey(survey);

		// Get OFC_SURVEY table id for name
		DialectAwareJooqFactory jf = getJooqFactory();
		int surveyId = 0;
		SelectConditionStep query = jf.select(OFC_SURVEY.ID).from(OFC_SURVEY)
				.where(OFC_SURVEY.NAME.equal(name));
		query.execute();
		Result<Record> result = query.getResult();

		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Checking survey");
		}
		if (result.isEmpty()) { // we should insert it now			
			surveyId = jf.nextId(OFC_SURVEY.ID, OFC_SURVEY_ID_SEQ);
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("    Survey " +  name + " not exist. Inserting with ID = " + surveyId );
			}
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
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("    Survey " +  name + " exist. Updating with ID = " + surveyId );
			}
			jf.update(OFC_SURVEY)
					.set(OFC_SURVEY.IDML, Factory.val(idml, SQLDataType.CLOB))
					.set(OFC_SURVEY.NAME, survey.getName())
					.set(OFC_SURVEY.URI, survey.getUri())
					.where(OFC_SURVEY.ID.equal(survey.getId())).execute();
		}

	}
	
	@Override
	protected CollectSurvey processSurveyRow(Record row) {
		try {
			if (row == null) {
				return null;
			}
			String idml = row.getValueAsString(OFC_SURVEY.IDML);
			CollectSurvey survey = unmarshalIdml(idml);
			survey.setId(row.getValueAsInteger(OFC_SURVEY.ID));
			survey.setName(row.getValue(OFC_SURVEY.NAME));
			return survey;
		} catch (IdmlParseException e) {
			throw new RuntimeException("Error deserializing IDML from database", e);
		}
	}
	
	@Override
	protected SurveySummary processSurveySummaryRow(Record row) {
		if (row == null) {
			return null;
		}
		Integer id = row.getValueAsInteger(OFC_SURVEY.ID);
		String name = row.getValue(OFC_SURVEY.NAME);
		String uri = row.getValue(OFC_SURVEY.URI);
		SurveySummary survey = new SurveySummary(id, name, uri);
		return survey;
	}
}
