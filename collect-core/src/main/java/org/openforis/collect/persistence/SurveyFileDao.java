package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_FILE_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Tables.OFC_SURVEY_FILE;

import java.util.Arrays;
import java.util.List;

import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.jooq.impl.DSL;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;
import org.openforis.collect.persistence.jooq.MappingDSLContext;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcSurveyFileRecord;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyFileDao extends MappingJooqDaoSupport<SurveyFile, SurveyFileDao.SurveyFileDSLContext> {
	
	public SurveyFileDao() {
		super(SurveyFileDSLContext.class);
	}

	public byte[] loadContent(SurveyFile item) {
		SurveyFileDSLContext dsl = dsl();
		ResultQuery<?> selectQuery = dsl.selectByIdQuery(item.getId());
		Record r = selectQuery.fetchOne();
		if ( r == null ) {
			return null;
		} else {
			return r.getValue(OFC_SURVEY_FILE.CONTENT);
		}
	}
	
	public List<SurveyFile> loadBySurvey(CollectSurvey survey) {
		SurveyFileDSLContext dsl = dsl(survey);
		Select<OfcSurveyFileRecord> select = 
			dsl.selectFrom(OFC_SURVEY_FILE)
				.where(OFC_SURVEY_FILE.SURVEY_ID.eq(survey.getId()))
				.orderBy(OFC_SURVEY_FILE.ID);
		Result<OfcSurveyFileRecord> result = select.fetch();
		return dsl.fromResult(result);
	}
	
	public void updateContent(SurveyFile item, byte[] content) {
		dsl().update(dsl().getTable())
			.set(OFC_SURVEY_FILE.CONTENT, content)
			.where(OFC_SURVEY_FILE.ID.eq(item.getId()))
			.execute();
	}
	
	public void copyItems(int fromSurveyId, int toSurveyId) {
		SurveyFileDSLContext dsl = dsl();
		int minId = loadMinId(dsl, fromSurveyId);
		int nextId = dsl.nextId();
		int idGap = nextId - minId;
		List<Field<?>> selectFields = Arrays.<Field<?>>asList(
				OFC_SURVEY_FILE.ID.add(idGap),
				DSL.val(toSurveyId, OFC_SURVEY_FILE.SURVEY_ID),
				OFC_SURVEY_FILE.TYPE, 
				OFC_SURVEY_FILE.FILENAME, 
				OFC_SURVEY_FILE.CONTENT
		);
		Select<?> select = dsl.select(selectFields)
			.from(OFC_SURVEY_FILE)
			.where(OFC_SURVEY_FILE.SURVEY_ID.equal(fromSurveyId))
			.orderBy(OFC_SURVEY_FILE.ID);
		Insert<OfcSurveyFileRecord> insert = dsl.insertInto(OFC_SURVEY_FILE, selectFields).select(select);
		insert.execute();
		restartIdSequence(dsl);
	}
	
	public void deleteBySurvey(int surveyId) {
		dsl().delete(OFC_SURVEY_FILE)
			.where(OFC_SURVEY_FILE.SURVEY_ID.equal(surveyId))
			.execute();
	}
	
	public void moveItems(int fromSurveyId, int toSurveyId) {
		dsl().update(OFC_SURVEY_FILE)
			.set(OFC_SURVEY_FILE.SURVEY_ID, toSurveyId)
			.where(OFC_SURVEY_FILE.SURVEY_ID.equal(fromSurveyId))
			.execute();
	}
	
	private void restartIdSequence(SurveyFileDSLContext jf) {
		int maxId = loadMaxId(jf);
		jf.restartSequence(OFC_SURVEY_FILE_ID_SEQ, maxId + 1);
	}
	
	private int loadMinId(SurveyFileDSLContext jf, int surveyId) {
		Integer result = jf.select(DSL.min(OFC_SURVEY_FILE.ID))
				.from(OFC_SURVEY_FILE)
				.where(OFC_SURVEY_FILE.SURVEY_ID.equal(surveyId))
				.fetchOne(0, Integer.class);
		return result == null ? 0: result.intValue();
	}

	private int loadMaxId(SurveyFileDSLContext jf) {
		Integer result = jf.select(DSL.max(OFC_SURVEY_FILE.ID))
				.from(OFC_SURVEY_FILE)
				.fetchOne(0, Integer.class);
		return result == null ? 0: result.intValue();
	}
	
	private SurveyFileDSLContext dsl(CollectSurvey survey) {
		return new SurveyFileDSLContext(getConfiguration(), survey);
	}

	public static class SurveyFileDSLContext extends MappingDSLContext<SurveyFile> {

		private static final long serialVersionUID = 1L;
		
		private CollectSurvey survey;
		
		public SurveyFileDSLContext(Configuration config, CollectSurvey survey) {
			super(config, OFC_SURVEY_FILE.ID, OFC_SURVEY_FILE_ID_SEQ, SurveyFile.class);
			this.survey = survey;
		}
		
		@Override
		protected SurveyFile newEntity() {
			return new SurveyFile(survey);
		}
		
		@Override
		protected void setId(SurveyFile o, int id) {
			o.setId(id);
		}

		@Override
		protected Integer getId(SurveyFile o) {
			return o.getId();
		}
		
		@Override
		protected void fromObject(SurveyFile o, StoreQuery<?> q) {
			q.addValue(OFC_SURVEY_FILE.SURVEY_ID, o.getSurvey().getId());
			q.addValue(OFC_SURVEY_FILE.FILENAME, o.getFilename());
			q.addValue(OFC_SURVEY_FILE.TYPE, o.getType().name());
		}
		
		@Override
		protected void fromRecord(Record r, SurveyFile o) {
			o.setFilename(r.getValue(OFC_SURVEY_FILE.FILENAME));
			o.setType(SurveyFileType.valueOf(r.getValue(OFC_SURVEY_FILE.TYPE)));
		}

	}
	
}
