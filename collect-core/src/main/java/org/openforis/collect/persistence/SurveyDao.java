package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcSurvey.OFC_SURVEY;

import java.util.ArrayList;
import java.util.List;

import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.persistence.jooq.tables.records.OfcSurveyRecord;
import org.openforis.commons.versioning.Version;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
@Transactional
public class SurveyDao extends SurveyBaseDao {
//	private final Log LOG = LogFactory.getLog(SurveyDao.class);

	@Transactional
	public void insert(CollectSurvey survey) throws SurveyImportException {
		String idml = marshalSurvey(survey);

		// Insert into OFC_SURVEY table
		CollectDSLContext dsl = dsl();
		int surveyId = dsl.nextId(OFC_SURVEY.ID, OFC_SURVEY_ID_SEQ);
		InsertSetMoreStep<OfcSurveyRecord> insert = dsl.insertInto(OFC_SURVEY)
				.set(OFC_SURVEY.ID, surveyId)
				.set(OFC_SURVEY.NAME, survey.getName())
				.set(OFC_SURVEY.URI, survey.getUri())
				.set(OFC_SURVEY.TEMPORARY, survey.isTemporary())
				.set(OFC_SURVEY.IDML, DSL.val(idml, SQLDataType.CLOB))
				.set(OFC_SURVEY.TARGET, survey.getTarget().getCode())
				.set(OFC_SURVEY.COLLECT_VERSION, survey.getCollectVersion().toString())
				.set(OFC_SURVEY.DATE_CREATED, toTimestamp(survey.getCreationDate()))
				.set(OFC_SURVEY.DATE_MODIFIED, toTimestamp(survey.getModifiedDate()))
				;
		insert.execute();

		survey.setId(surveyId);
	}

	public CollectSurvey loadById(int id) {
		Record record = dsl()
				.select()
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.ID.equal(id))
				.fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}
	
	public CollectSurvey loadByUri(String uri) {
		return loadByUri(uri, false);
	}

	public CollectSurvey loadByUri(String uri, boolean temporary) {
		Record record = dsl()
				.select()
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.URI.equal(uri).and(OFC_SURVEY.TEMPORARY.equal(temporary)))
				.fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}
	
	public CollectSurvey loadByName(String name) {
		return loadByName(name, false);
	}
	
	public CollectSurvey loadByName(String name, boolean temporary) {
		Record record = dsl()
				.select()
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.NAME.equal(name).and(OFC_SURVEY.TEMPORARY.equal(temporary)))
				.fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}
	
	@Transactional
	public List<SurveySummary> loadTemporarySummaries() {
		List<SurveySummary> surveys = new ArrayList<SurveySummary>();
		Result<Record> results = dsl().select()
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.TEMPORARY.equal(true))
				.fetch();
		for (Record row : results) {
			SurveySummary survey = processSurveySummaryRow(row);
			if (survey != null) {
				surveys.add(survey);
			}
		}
		return surveys;
	}
	
	public SurveySummary loadSurveySummary(int id) {
		Record record = dsl().select()
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.ID.equal(id))
				.fetchOne();
		SurveySummary result = processSurveySummaryRow(record);
		return result;
	}
	
	public SurveySummary loadSurveySummaryByName(String name) {
		return loadSurveySummaryByName(name, false);
	}

	public SurveySummary loadSurveySummaryByName(String name, boolean temporary) {
		Record record = dsl().select()
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.NAME.equal(name).and(OFC_SURVEY.TEMPORARY.equal(temporary)))
				.fetchOne();
		SurveySummary result = processSurveySummaryRow(record);
		return result;
	}
	
	public SurveySummary loadSurveySummaryByUri(String uri) {
		return loadSurveySummaryByUri(uri, false);
	}
	
	public SurveySummary loadSurveySummaryByUri(String uri, boolean temporary) {
		Record record = dsl().select()
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.URI.equal(uri).and(OFC_SURVEY.TEMPORARY.equal(temporary)))
				.fetchOne();
		SurveySummary result = processSurveySummaryRow(record);
		return result;
	}
	
	@Transactional
	public List<CollectSurvey> loadAll() {
		List<CollectSurvey> surveys = new ArrayList<CollectSurvey>();
		Result<Record> results = dsl()
				.select()
				.from(OFC_SURVEY)
				.fetch();
		for (Record row : results) {
			CollectSurvey survey = processSurveyRow(row);
			surveys.add(survey);
		}
		return surveys;
	}

	@Transactional
	public List<CollectSurvey> loadAllPublished() {
		List<CollectSurvey> surveys = new ArrayList<CollectSurvey>();
		Result<Record> results = dsl()
				.select()
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.TEMPORARY.equal(false))
				.fetch();
		for (Record row : results) {
			CollectSurvey survey = processSurveyRow(row);
			surveys.add(survey);
		}
		return surveys;
	}
	
	public void delete(int id) {
		dsl().delete(OFC_SURVEY)
			.where(OFC_SURVEY.ID.equal(id))
			.execute();
	}

	public void update(CollectSurvey survey) throws SurveyImportException {
		String idml = marshalSurvey(survey);

		dsl().update(OFC_SURVEY)
				.set(OFC_SURVEY.TEMPORARY, survey.isTemporary())
				.set(OFC_SURVEY.IDML, DSL.val(idml, SQLDataType.CLOB))
				.set(OFC_SURVEY.TARGET, survey.getTarget().getCode())
				.set(OFC_SURVEY.COLLECT_VERSION, survey.getCollectVersion().toString())
				.set(OFC_SURVEY.DATE_CREATED, toTimestamp(survey.getCreationDate()))
				.set(OFC_SURVEY.DATE_MODIFIED, toTimestamp(survey.getModifiedDate()))
				.where(OFC_SURVEY.ID.equal(survey.getId())).execute();
	}

	@Override
	protected CollectSurvey processSurveyRow(Record row) {
		try {
			if (row == null) {
				return null;
			}
			String idml = row.getValue(OFC_SURVEY.IDML);
			CollectSurvey survey = unmarshalIdml(idml);
			survey.setId(row.getValue(OFC_SURVEY.ID));
			survey.setName(row.getValue(OFC_SURVEY.NAME));
			survey.setTemporary(row.getValue(OFC_SURVEY.TEMPORARY));
			survey.setTarget(SurveyTarget.fromCode(row.getValue(OFC_SURVEY.TARGET)));
			survey.setCreationDate(row.getValue(OFC_SURVEY.DATE_CREATED));
			survey.setModifiedDate(row.getValue(OFC_SURVEY.DATE_MODIFIED));
			survey.setCollectVersion(new Version(row.getValue(OFC_SURVEY.COLLECT_VERSION)));
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
		Integer id = row.getValue(OFC_SURVEY.ID);
		String name = row.getValue(OFC_SURVEY.NAME);
		String uri = row.getValue(OFC_SURVEY.URI);
		SurveySummary survey = new SurveySummary(id, name, uri);
		survey.setTemporary(row.getValue(OFC_SURVEY.TEMPORARY));
		survey.setTarget(SurveyTarget.fromCode(row.getValue(OFC_SURVEY.TARGET)));
		survey.setCreationDate(row.getValue(OFC_SURVEY.DATE_CREATED));
		survey.setModifiedDate(row.getValue(OFC_SURVEY.DATE_MODIFIED));
		return survey;
	}
}
