package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcSurvey.OFC_SURVEY;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
	
	@Transactional
	public List<SurveySummary> loadSummaries() {
		Factory jf = getJooqFactory();
		List<SurveySummary> surveys = new ArrayList<SurveySummary>();
		Result<Record> results = jf.select().from(OFC_SURVEY).fetch();
		for (Record row : results) {
			SurveySummary survey = processSurveySummaryRow(row);
			if (survey != null) {
				surveys.add(survey);
			}
		}
		return surveys;
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

	public void delete(int id) {
		DialectAwareJooqFactory jf = getJooqFactory();
		jf.delete(OFC_SURVEY)
			.where(OFC_SURVEY.ID.equal(id))
			.execute();
	}

	public void updateModel(CollectSurvey survey) throws SurveyImportException {
		//validate name
		if (StringUtils.isBlank(survey.getName())) {
			throw new SurveyImportException(
					"Survey name must be set before importing");
		}
		DialectAwareJooqFactory jf = getJooqFactory();

		// Get OFC_SURVEY record id by survey uri
		Integer oldSurveyId = getSurveyId(jf, survey.getUri());
		if ( oldSurveyId == null ) {
			throw new SurveyImportException(String.format("Published survey with uri %s not found", survey.getUri()));
		}
		survey.setId(oldSurveyId);
		
		String idml = marshalSurvey(survey);

		jf.update(OFC_SURVEY)
				.set(OFC_SURVEY.IDML, Factory.val(idml, SQLDataType.CLOB))
				.where(OFC_SURVEY.ID.equal(survey.getId())).execute();
	}

	private Integer getSurveyId(DialectAwareJooqFactory jf, String uri) {
		SelectConditionStep query = jf
				.select(OFC_SURVEY.ID)
				.from(OFC_SURVEY)
				.where(OFC_SURVEY.URI.equal(uri));
		query.execute();
		Result<Record> result = query.getResult();

		Record record = result.get(0);			
		Integer surveyId = record.getValueAsInteger(OFC_SURVEY.ID);

		return surveyId;
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
