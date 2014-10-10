package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcSurvey.OFC_SURVEY;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
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
//	private final Log LOG = LogFactory.getLog(SurveyDao.class);

	@Transactional
	public void importModel(Survey survey) throws SurveyImportException {
		String name = survey.getName();
		if (StringUtils.isBlank(name)) {
			throw new SurveyImportException(
					"Survey name must be set before importing");
		}

		String idml = marshalSurvey(survey);

		// Insert into OFC_SURVEY table
		CollectDSLContext dsl = dsl();
		int surveyId = dsl.nextId(OFC_SURVEY.ID, OFC_SURVEY_ID_SEQ);
		dsl.insertInto(OFC_SURVEY).set(OFC_SURVEY.ID, surveyId)				
				.set(OFC_SURVEY.NAME, survey.getName())
				.set(OFC_SURVEY.URI, survey.getUri())
				.set(OFC_SURVEY.IDML, DSL.val(idml, SQLDataType.CLOB))
				.execute();

		survey.setId(surveyId);
	}

	public Survey load(int id) {
		Record record = dsl()
				.select()
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.ID.equal(id)).fetchOne();
		Survey survey = processSurveyRow(record);
		return survey;
	}

	public CollectSurvey load(String name) {
		Record record = dsl()
				.select()
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.NAME.equal(name))
				.fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}

	public CollectSurvey loadByUri(String uri) {
		Record record = dsl().select().from(OFC_SURVEY)
				.where(OFC_SURVEY.URI.equal(uri)).fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}
	
	@Transactional
	public List<SurveySummary> loadSummaries() {
		List<SurveySummary> surveys = new ArrayList<SurveySummary>();
		Result<Record> results = dsl().select()
				.from(OFC_SURVEY)
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
		Record record = dsl().select()
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.NAME.equal(name))
				.fetchOne();
		SurveySummary result = processSurveySummaryRow(record);
		return result;
	}
	
	@Transactional
	public List<CollectSurvey> loadAll() {
		List<CollectSurvey> surveys = new ArrayList<CollectSurvey>();
		Result<Record> results = dsl().select().from(OFC_SURVEY).fetch();
		for (Record row : results) {
			CollectSurvey survey = processSurveyRow(row);
			if (survey != null) {
				//loadNodeDefinitions(survey);
				surveys.add(survey);
			}
		}
		return surveys;
	}

	public void delete(int id) {
		dsl().delete(OFC_SURVEY)
			.where(OFC_SURVEY.ID.equal(id))
			.execute();
	}

	public void updateModel(CollectSurvey survey) throws SurveyImportException {
		//validate name
		if (StringUtils.isBlank(survey.getName())) {
			throw new SurveyImportException(
					"Survey name must be set before importing");
		}
		// Get OFC_SURVEY record id by survey uri
		CollectDSLContext dsl = dsl();
		Integer oldSurveyId = getSurveyId(dsl, survey.getUri());
		if ( oldSurveyId == null ) {
			throw new SurveyImportException(String.format("Published survey with uri %s not found", survey.getUri()));
		}
		survey.setId(oldSurveyId);
		
		String idml = marshalSurvey(survey);

		dsl.update(OFC_SURVEY)
				.set(OFC_SURVEY.IDML, DSL.val(idml, SQLDataType.CLOB))
				.where(OFC_SURVEY.ID.equal(survey.getId())).execute();
	}

	private Integer getSurveyId(CollectDSLContext dsl, String uri) {
		SelectConditionStep<Record1<Integer>> query = dsl
				.select(OFC_SURVEY.ID)
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.URI.equal(uri));
		query.execute();
		Result<Record1<Integer>> result = query.getResult();

		Record record = result.get(0);			
		Integer surveyId = record.getValue(OFC_SURVEY.ID);
		return surveyId;
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
		return survey;
	}
}
