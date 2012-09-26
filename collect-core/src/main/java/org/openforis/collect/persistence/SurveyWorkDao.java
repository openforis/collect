package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_WORK_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcSurveyWork.OFC_SURVEY_WORK;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.jooq.impl.SQLDataType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.Survey;
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
	public void insert(Survey survey) throws SurveyImportException {
		String idml = marshalSurvey(survey);
		Factory jf = getJooqFactory();
		int surveyId = jf.nextval(OFC_SURVEY_WORK_ID_SEQ).intValue();
		jf.insertInto(OFC_SURVEY_WORK).set(OFC_SURVEY_WORK.ID, surveyId)				
				.set(OFC_SURVEY_WORK.NAME, survey.getName())
				.set(OFC_SURVEY_WORK.URI, survey.getUri())
				.set(OFC_SURVEY_WORK.IDML, Factory.val(idml, SQLDataType.CLOB))
				.execute();

		survey.setId(surveyId);
	}
	
	@Transactional
	public void update(Survey survey) throws SurveyImportException {
		String idml = marshalSurvey(survey);
		Factory jf = getJooqFactory();
		Integer id = survey.getId();
		jf.update(OFC_SURVEY_WORK)
			.set(OFC_SURVEY_WORK.IDML, Factory.val(idml, SQLDataType.CLOB))
			.set(OFC_SURVEY_WORK.NAME, survey.getName())
			.set(OFC_SURVEY_WORK.URI, survey.getUri())
			.where(OFC_SURVEY_WORK.ID.equal(id)).execute();
	}

}
