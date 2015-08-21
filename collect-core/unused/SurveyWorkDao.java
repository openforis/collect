package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_WORK_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcSurvey.OFC_SURVEY;
import static org.openforis.collect.persistence.jooq.tables.OfcSurveyWork.OFC_SURVEY_WORK;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.commons.versioning.Version;
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
		Record record = dsl().select().from(OFC_SURVEY_WORK)
				.where(OFC_SURVEY_WORK.ID.equal(id)).fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}

	public CollectSurvey loadByUri(String uri) {
		Record record = dsl().select().from(OFC_SURVEY_WORK)
				.where(OFC_SURVEY_WORK.URI.equal(uri)).fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}
	
	public CollectSurvey loadByName(String name) {
		Record record = dsl().select().from(OFC_SURVEY_WORK)
				.where(OFC_SURVEY_WORK.NAME.equal(name)).fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}
	
	@Transactional
	public List<CollectSurvey> loadAll() {
		List<CollectSurvey> surveys = new ArrayList<CollectSurvey>();
		Result<Record> results = dsl().select().from(OFC_SURVEY_WORK).fetch();
		for (Record row : results) {
			CollectSurvey survey = processSurveyRow(row);
			if (survey != null) {
				surveys.add(survey);
			}
		}
		return surveys;
	}
	
	@Transactional
	public List<SurveySummary> loadSummaries() {
		List<SurveySummary> surveys = new ArrayList<SurveySummary>();
		Result<Record> results = dsl().select().from(OFC_SURVEY_WORK).fetch();
		for (Record row : results) {
			SurveySummary survey = processSurveySummaryRow(row);
			if (survey != null) {
				surveys.add(survey);
			}
		}
		return surveys;
	}
	
	@Transactional
	public SurveySummary loadSurveySummary(int id) {
		Record record = dsl().select()
				.from(OFC_SURVEY_WORK)
				.where(OFC_SURVEY_WORK.ID.equal(id))
				.fetchOne();
		SurveySummary result = processSurveySummaryRow(record);
		return result;
	}

	@Transactional
	public SurveySummary loadSurveySummaryByName(String name) {
		Record record = dsl().select()
				.from(OFC_SURVEY_WORK)
				.where(OFC_SURVEY_WORK.NAME.equal(name))
				.fetchOne();
		SurveySummary result = processSurveySummaryRow(record);
		return result;
	}

	@Transactional
	public SurveySummary loadSurveySummaryByUri(String uri) {
		Record record = dsl().select()
				.from(OFC_SURVEY_WORK)
				.where(OFC_SURVEY_WORK.URI.equal(uri))
				.fetchOne();
		SurveySummary result = processSurveySummaryRow(record);
		return result;
	}

	@Transactional
	public void insert(CollectSurvey survey) throws SurveyImportException {
		String idml = marshalSurvey(survey);
		CollectDSLContext dsl = dsl();
		int surveyId = dsl.nextId(OFC_SURVEY_WORK.ID, OFC_SURVEY_WORK_ID_SEQ);
		dsl().insertInto(OFC_SURVEY_WORK).set(OFC_SURVEY_WORK.ID, surveyId)				
				.set(OFC_SURVEY_WORK.NAME, survey.getName())
				.set(OFC_SURVEY_WORK.URI, survey.getUri())
				.set(OFC_SURVEY.TARGET, survey.getTarget().getCode())
				.set(OFC_SURVEY.COLLECT_VERSION, survey.getCollectVersion().toString())
				.set(OFC_SURVEY.DATE_CREATED, toTimestamp(survey.getCreationDate()))
				.set(OFC_SURVEY.DATE_MODIFIED, toTimestamp(survey.getModifiedDate()))
				.set(OFC_SURVEY_WORK.IDML, DSL.val(idml, SQLDataType.CLOB))
				.execute();

		survey.setId(surveyId);
	}
	
	@Transactional
	public void update(CollectSurvey survey) throws SurveyImportException {
		String idml = marshalSurvey(survey);
		Integer id = survey.getId();
		dsl().update(OFC_SURVEY_WORK)
			.set(OFC_SURVEY_WORK.IDML, DSL.val(idml, SQLDataType.CLOB))
			.set(OFC_SURVEY_WORK.NAME, survey.getName())
			.set(OFC_SURVEY_WORK.URI, survey.getUri())
			.set(OFC_SURVEY.TARGET, survey.getTarget().getCode())
			.set(OFC_SURVEY.COLLECT_VERSION, survey.getCollectVersion().toString())
			.set(OFC_SURVEY.DATE_CREATED, toTimestamp(survey.getCreationDate()))
			.set(OFC_SURVEY.DATE_MODIFIED, toTimestamp(survey.getModifiedDate()))
			.where(OFC_SURVEY_WORK.ID.equal(id)).execute();
	}

	@Transactional
	public void delete(CollectSurvey survey) {
		Integer id = survey.getId();
		delete(id);
	}

	@Transactional
	public void delete(int id) {
		dsl().delete(OFC_SURVEY_WORK)
			.where(OFC_SURVEY_WORK.ID.equal(id)).execute();
	}

	@Override
	protected CollectSurvey processSurveyRow(Record row) {
		if (row == null) {
			return null;
		}
		String name = null;
		Integer id = null;
		try {
			id = row.getValue(OFC_SURVEY_WORK.ID);
			name = row.getValue(OFC_SURVEY_WORK.NAME);
			String idml = row.getValue(OFC_SURVEY_WORK.IDML);
			CollectSurvey survey = unmarshalIdml(idml);
			survey.setId(id);
			survey.setName(name);
			survey.setWork(true);
			survey.setTarget(SurveyTarget.fromCode(row.getValue(OFC_SURVEY.TARGET)));
			survey.setCreationDate(row.getValue(OFC_SURVEY.DATE_CREATED));
			survey.setModifiedDate(row.getValue(OFC_SURVEY.DATE_MODIFIED));
			survey.setCollectVersion(new Version(row.getValue(OFC_SURVEY.COLLECT_VERSION)));
			return survey;
		} catch (IdmlParseException e) {
			throw new RuntimeException(String.format("Error deserializing temporary survey from database (id=%d, name=%s)", id, name), e);
		}
	}
	
	@Override
	protected SurveySummary processSurveySummaryRow(Record row) {
		if (row == null) {
			return null;
		}
		Integer id = row.getValue(OFC_SURVEY_WORK.ID);
		String name = row.getValue(OFC_SURVEY_WORK.NAME);
		String uri = row.getValue(OFC_SURVEY.URI);
		SurveySummary survey = new SurveySummary(id, name, uri, null);
		survey.setTarget(SurveyTarget.fromCode(row.getValue(OFC_SURVEY.TARGET)));
		survey.setCreationDate(row.getValue(OFC_SURVEY.DATE_CREATED));
		survey.setModifiedDate(row.getValue(OFC_SURVEY.DATE_MODIFIED));
		survey.setWork(true);
		survey.setPublished(false);
		return survey;
	}

}
