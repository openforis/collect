package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_FILE_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Tables.OFC_SURVEY_FILE;

import java.util.Arrays;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.openforis.collect.Environment;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;
import org.openforis.collect.persistence.SurveyFileDao.SurveyFileDSLContext;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.OfcSurveyFile;
import org.openforis.collect.persistence.jooq.tables.records.OfcSurveyFileRecord;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyFileDao extends SurveyObjectMappingJooqDaoSupport<Integer, SurveyFile, SurveyFileDSLContext> {
	
	private static final int BLOB_CHUNK_SIZE = 1000000;

	private static final Field<?>[] SUMMARY_FIELDS = new Field<?>[] {
		OFC_SURVEY_FILE.ID,
		OFC_SURVEY_FILE.SURVEY_ID, 
		OFC_SURVEY_FILE.TYPE,
		OFC_SURVEY_FILE.FILENAME
	};
	
	private static final Field<?>[] ALL_FIELDS = new Field<?>[] {
		OFC_SURVEY_FILE.ID,
		OFC_SURVEY_FILE.SURVEY_ID, 
		OFC_SURVEY_FILE.TYPE,
		OFC_SURVEY_FILE.FILENAME, 
		OFC_SURVEY_FILE.CONTENT
	};
	
	public SurveyFileDao() {
		super(SurveyFileDSLContext.class);
	}
	
	public byte[] loadContent(SurveyFile item) {
		OfcSurveyFile table = OFC_SURVEY_FILE;
		TableField<OfcSurveyFileRecord, byte[]> field = table.CONTENT;
		Condition condition = table.ID.eq(item.getId());
		if (Environment.isAndroid()) {
			return readInChunks(table, field, condition);
		} else {
			Record record = dsl()
					.select(field)
					.from(table)
					.where(condition)
					.fetchOne();
			return record == null ? null : record.getValue(OFC_SURVEY_FILE.CONTENT);
		}
	}

	
	@Override
	public List<SurveyFile> loadBySurvey(CollectSurvey survey) {
		SurveyFileDSLContext dsl = dsl(survey);
		Select<Record> select = dsl.select(SUMMARY_FIELDS)
				.from(OFC_SURVEY_FILE)
				.where(OFC_SURVEY_FILE.SURVEY_ID.eq(survey.getId()))
				.orderBy(OFC_SURVEY_FILE.ID);
		Result<Record> result = select.fetch();
		return dsl.fromResult(result);
	}
	
	@Override
	public void deleteBySurvey(CollectSurvey survey) {
		deleteBySurvey(survey.getId());
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
		Insert<OfcSurveyFileRecord> insert = dsl.insertInto(OFC_SURVEY_FILE, ALL_FIELDS).select(select);
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
	
	private byte[] readInChunks(Table<?> table, TableField<?, byte[]> field,
			Condition condition) {
		CollectDSLContext dsl = dsl();
		int size = dsl
				.select(DSL.field(String.format("length(%s)", field.getName()), Integer.class))
				.from(table)
				.where(condition)
				.fetchOne()
				.getValue(0, Integer.class);
		byte[] content = new byte[size];
		
		int startingPosition = 1;
		while (startingPosition <= size) {
			byte[] part = (byte[]) dsl
					.select(DSL.field(String.format("substr(%s, %d, %d)", field.getName(), startingPosition, BLOB_CHUNK_SIZE)))
					.from(table)
					.where(condition)
					.fetchOne()
					.getValue(0);
			//copy into content array
			for (int i = 0; i < part.length; i++) {
				content[startingPosition - 1 + i] = part[i];
			}
			startingPosition += BLOB_CHUNK_SIZE;
		}
		return content;
	}
	
	public static class SurveyFileDSLContext extends SurveyObjectMappingDSLContext<Integer, SurveyFile> {

		private static final long serialVersionUID = 1L;
		
		public SurveyFileDSLContext(Configuration config) {
			this(config, null);
		}
		
		public SurveyFileDSLContext(Configuration config, CollectSurvey survey) {
			super(config, OFC_SURVEY_FILE.ID, OFC_SURVEY_FILE_ID_SEQ, SurveyFile.class, survey);
			this.survey = survey;
		}
		
		@Override
		protected SurveyFile newEntity() {
			return new SurveyFile(survey);
		}
		
		@Override
		protected void setId(SurveyFile o, Integer id) {
			o.setId(id);
		}

		@Override
		protected Integer getId(SurveyFile o) {
			return o.getId();
		}
		
		@Override
		protected void fromObject(SurveyFile o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_SURVEY_FILE.SURVEY_ID, o.getSurvey().getId());
			q.addValue(OFC_SURVEY_FILE.FILENAME, o.getFilename());
			q.addValue(OFC_SURVEY_FILE.TYPE, o.getType().getCode());
		}
		
		@Override
		protected void fromRecord(Record r, SurveyFile o) {
			super.fromRecord(r, o);
			o.setFilename(r.getValue(OFC_SURVEY_FILE.FILENAME));
			o.setType(SurveyFileType.fromCode(r.getValue(OFC_SURVEY_FILE.TYPE)));
		}

	}
	
}
