package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_WORK_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcSurvey.OFC_SURVEY;
import static org.openforis.collect.persistence.jooq.tables.OfcSurveyWork.OFC_SURVEY_WORK;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.jooq.impl.SQLDataType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 * 
 */
@Transactional
public class SurveyWorkDao extends SurveyBaseDao {
	// private final Log LOG = LogFactory.getLog(SurveyWorkDao.class);

	public CollectSurvey load(int id) {
		Factory jf = getJooqFactory();
		Record record = jf.select().from(OFC_SURVEY_WORK)
				.where(OFC_SURVEY_WORK.ID.equal(id)).fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}

	public CollectSurvey loadByUri(String uri) {
		Factory jf = getJooqFactory();
		Record record = jf.select().from(OFC_SURVEY_WORK)
				.where(OFC_SURVEY_WORK.URI.equal(uri)).fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}
	
	@Transactional
	public List<CollectSurvey> loadAll() {
		Factory jf = getJooqFactory();
		List<CollectSurvey> surveys = new ArrayList<CollectSurvey>();
		Result<Record> results = jf.select().from(OFC_SURVEY_WORK).fetch();
		for (Record row : results) {
			CollectSurvey survey = processSurveyRow(row);
			if (survey != null) {
				//loadNodeDefinitions(survey);
				surveys.add(survey);
			}
		}
		return surveys;
	}
	
	@Transactional
	public List<SurveySummary> loadSummaries() {
		Factory jf = getJooqFactory();
		List<SurveySummary> surveys = new ArrayList<SurveySummary>();
		Result<Record> results = jf.select().from(OFC_SURVEY_WORK).fetch();
		for (Record row : results) {
			SurveySummary survey = processSurveySummaryRow(row);
			if (survey != null) {
				//loadNodeDefinitions(survey);
				surveys.add(survey);
			}
		}
		return surveys;
	}
	
	@Transactional
	public SurveySummary loadSurveySummary(int id) {
		Factory jf = getJooqFactory();
		Record record = jf.select()
				.from(OFC_SURVEY_WORK)
				.where(OFC_SURVEY_WORK.ID.equal(id))
				.fetchOne();
		SurveySummary result = processSurveySummaryRow(record);
		return result;
	}

	@Transactional
	public SurveySummary loadSurveySummaryByName(String name) {
		Factory jf = getJooqFactory();
		Record record = jf.select()
				.from(OFC_SURVEY_WORK)
				.where(OFC_SURVEY_WORK.NAME.equal(name))
				.fetchOne();
		SurveySummary result = processSurveySummaryRow(record);
		return result;
	}

	@Transactional
	public void insert(CollectSurvey survey) throws SurveyImportException {
		String idml = marshalSurvey(survey);
		DialectAwareJooqFactory jf = getJooqFactory();
		int surveyId = jf.nextId(OFC_SURVEY_WORK.ID, OFC_SURVEY_WORK_ID_SEQ);
		jf.insertInto(OFC_SURVEY_WORK).set(OFC_SURVEY_WORK.ID, surveyId)				
				.set(OFC_SURVEY_WORK.NAME, survey.getName())
				.set(OFC_SURVEY_WORK.URI, survey.getUri())
				.set(OFC_SURVEY_WORK.IDML, Factory.val(idml, SQLDataType.CLOB))
				.execute();

		survey.setId(surveyId);
	}
	
	@Transactional
	public void update(CollectSurvey survey) throws SurveyImportException {
		String idml = marshalSurvey(survey);
		Factory jf = getJooqFactory();
		Integer id = survey.getId();
		jf.update(OFC_SURVEY_WORK)
			.set(OFC_SURVEY_WORK.IDML, Factory.val(idml, SQLDataType.CLOB))
			.set(OFC_SURVEY_WORK.NAME, survey.getName())
			.set(OFC_SURVEY_WORK.URI, survey.getUri())
			.where(OFC_SURVEY_WORK.ID.equal(id)).execute();
	}

	@Transactional
	public void delete(CollectSurvey survey) {
		Integer id = survey.getId();
		delete(id);
	}

	@Transactional
	public void delete(int id) {
		Factory jf = getJooqFactory();
		jf.delete(OFC_SURVEY_WORK)
			.where(OFC_SURVEY_WORK.ID.equal(id)).execute();
	}

	@Override
	protected CollectSurvey processSurveyRow(Record row) {
		try {
			if (row == null) {
				return null;
			}
			String idml = row.getValueAsString(OFC_SURVEY_WORK.IDML);
			CollectSurvey survey = unmarshalIdml(idml);
			survey.setId(row.getValueAsInteger(OFC_SURVEY_WORK.ID));
			survey.setName(row.getValue(OFC_SURVEY_WORK.NAME));
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
		Integer id = row.getValueAsInteger(OFC_SURVEY_WORK.ID);
		String name = row.getValue(OFC_SURVEY_WORK.NAME);
		String uri = row.getValue(OFC_SURVEY.URI);
		SurveySummary survey = new SurveySummary(id, name, uri);
		return survey;
	}

}
