package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcSurvey.OFC_SURVEY;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.StoreQuery;
import org.jooq.UpdateQuery;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.openforis.collect.manager.SurveyMigrator;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcSurveyRecord;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.commons.versioning.Version;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class SurveyDao extends JooqDaoSupport {

	@Autowired
	protected CollectSurveyIdmlBinder surveySerializer;
	
	public void init() {
	}
	
	public CollectSurvey unmarshalIdml(String idml) throws IdmlParseException {
		try {
			byte[] bytes = idml.getBytes(OpenForisIOUtils.UTF_8);
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			return unmarshalIdml(is);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public CollectSurvey unmarshalIdml(InputStream is) throws IdmlParseException {
		return unmarshalIdml(OpenForisIOUtils.toReader(is));
	}
	
	public CollectSurvey unmarshalIdml(InputStream is, boolean includeCodeListItems) throws IdmlParseException {
		return unmarshalIdml(OpenForisIOUtils.toReader(is), includeCodeListItems);
	}

	public CollectSurvey unmarshalIdml(Reader reader) throws IdmlParseException {
		return unmarshalIdml(reader, true);
	}
	
	public CollectSurvey unmarshalIdml(Reader reader, boolean includeCodeListItems) throws IdmlParseException {
		try {
			CollectSurvey survey = (CollectSurvey) surveySerializer.unmarshal(reader, includeCodeListItems);
			SurveyMigrator migrator = getSurveyMigrator();
			if (migrator.isMigrationNeeded(survey)) {
				migrator.migrate(survey);
			}
			return survey;
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	public String marshalSurvey(Survey survey) throws SurveyImportException {
		return surveySerializer.marshal(survey);
	}
	
	public void insert(CollectSurvey survey) throws SurveyImportException {
		CollectDSLContext dsl = dsl();
		
		//fetch next id
		int surveyId = dsl.nextId(OFC_SURVEY.ID, OFC_SURVEY_ID_SEQ);
		
		InsertQuery<OfcSurveyRecord> insert = dsl.insertQuery(OFC_SURVEY);
		addNewSurveyValues(insert, survey, surveyId);
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
		UpdateQuery<OfcSurveyRecord> update = dsl().updateQuery(OFC_SURVEY);
		addUpdateValues(update, survey);
		update.addConditions(OFC_SURVEY.ID.equal(survey.getId()));
		update.execute();
	}

	private void addUpdateValues(StoreQuery<OfcSurveyRecord> storeQuery,
			CollectSurvey survey) throws SurveyImportException {
		String idml = marshalSurvey(survey);
		storeQuery.addValue(OFC_SURVEY.TEMPORARY, survey.isTemporary());
		storeQuery.addValue(OFC_SURVEY.PUBLISHED_ID, survey.getPublishedId());
		storeQuery.addValue(OFC_SURVEY.IDML, DSL.val(idml, SQLDataType.CLOB));
		storeQuery.addValue(OFC_SURVEY.TARGET, survey.getTarget().getCode());
		storeQuery.addValue(OFC_SURVEY.COLLECT_VERSION, survey.getCollectVersion().toString());
		storeQuery.addValue(OFC_SURVEY.DATE_CREATED, toTimestamp(survey.getCreationDate()));
		storeQuery.addValue(OFC_SURVEY.DATE_MODIFIED, toTimestamp(survey.getModifiedDate()));
		storeQuery.addValue(OFC_SURVEY.INSTITUTION_ID, survey.getUserGroupId());
	}

	private void addNewSurveyValues(StoreQuery<OfcSurveyRecord> storeQuery,
			CollectSurvey survey, int surveyId) throws SurveyImportException {
		storeQuery.addValue(OFC_SURVEY.ID, surveyId);
		storeQuery.addValue(OFC_SURVEY.NAME, survey.getName());
		storeQuery.addValue(OFC_SURVEY.URI, survey.getUri());
		addUpdateValues(storeQuery, survey);
	}
	
	protected CollectSurvey processSurveyRow(Record row) {
		try {
			if (row == null) {
				return null;
			}
			String idml = row.getValue(OFC_SURVEY.IDML);
			CollectSurvey survey = unmarshalIdml(idml);
			survey.setCollectVersion(new Version(row.getValue(OFC_SURVEY.COLLECT_VERSION)));
			survey.setCreationDate(row.getValue(OFC_SURVEY.DATE_CREATED));
			survey.setId(row.getValue(OFC_SURVEY.ID));
			survey.setUserGroupId(row.getValue(OFC_SURVEY.INSTITUTION_ID));
			survey.setModifiedDate(row.getValue(OFC_SURVEY.DATE_MODIFIED));
			survey.setName(row.getValue(OFC_SURVEY.NAME));
			survey.setPublishedId(row.getValue(OFC_SURVEY.PUBLISHED_ID));
			survey.setTarget(SurveyTarget.fromCode(row.getValue(OFC_SURVEY.TARGET)));
			survey.setTemporary(row.getValue(OFC_SURVEY.TEMPORARY));
			survey.setUri(row.getValue(OFC_SURVEY.URI));
			return survey;
		} catch (IdmlParseException e) {
			throw new RuntimeException("Error deserializing IDML from database", e);
		}
	}
	
	protected SurveySummary processSurveySummaryRow(Record row) {
		if (row == null) {
			return null;
		}
		Integer id = row.getValue(OFC_SURVEY.ID);
		String name = row.getValue(OFC_SURVEY.NAME);
		String uri = row.getValue(OFC_SURVEY.URI);
		SurveySummary summary = new SurveySummary(id, name, uri);
		summary.setTemporary(row.getValue(OFC_SURVEY.TEMPORARY));
		summary.setPublishedId(row.getValue(OFC_SURVEY.PUBLISHED_ID));
		summary.setTarget(SurveyTarget.fromCode(row.getValue(OFC_SURVEY.TARGET)));
		summary.setCreationDate(row.getValue(OFC_SURVEY.DATE_CREATED));
		summary.setModifiedDate(row.getValue(OFC_SURVEY.DATE_MODIFIED));
		summary.setUserGroupId(row.getValue(OFC_SURVEY.INSTITUTION_ID));
		return summary;
	}
	
	protected SurveyMigrator getSurveyMigrator() {
		return new SurveyMigrator();
	}
	
	public CollectSurveyIdmlBinder getSurveySerializer() {
		return surveySerializer;
	}
	
	public void setSurveySerializer(CollectSurveyIdmlBinder surveySerializer) {
		this.surveySerializer = surveySerializer;
	}
}

