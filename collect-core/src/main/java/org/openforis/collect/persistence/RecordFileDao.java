package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.tables.OfcRecordFile.OFC_RECORD_FILE;

import java.util.Date;
import java.util.List;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.openforis.collect.model.RecordFile;
import org.openforis.collect.persistence.jooq.tables.daos.OfcRecordFileDao;
import org.openforis.collect.persistence.utils.Daos;
/**
 * 
 * @author S. Ricci
 *
 */
public class RecordFileDao extends OfcRecordFileDao {

	private static final Field<?>[] SUMMARY_FIELDS = new Field<?>[] {
			OFC_RECORD_FILE.UUID,
			OFC_RECORD_FILE.ORIGINAL_NAME,
			OFC_RECORD_FILE.RECORD_ID,
			OFC_RECORD_FILE.CREATED_BY_ID,
			OFC_RECORD_FILE.DATE_CREATED,
			OFC_RECORD_FILE.MODIFIED_BY_ID,
			OFC_RECORD_FILE.DATE_MODIFIED
	};
	
	public RecordFileDao(Configuration configuration) {
		super(configuration);
	}
	
	public byte[] loadContentByUuid(String uuid) {
		Record1<byte[]> record = dsl()
			.select(OFC_RECORD_FILE.CONTENT)
			.from(OFC_RECORD_FILE)
			.where(OFC_RECORD_FILE.UUID.eq(uuid))
			.fetchOne();
		return record == null ? null : (byte[]) record.getValue(0);
	}
	
	public RecordFile loadSummaryByUuid(String uuid) {
		RecordFile recordFile = dsl()
			.select(SUMMARY_FIELDS)
			.from(OFC_RECORD_FILE)
			.where(OFC_RECORD_FILE.UUID.eq(uuid))
			.fetchOneInto(RecordFile.class);
		return recordFile;
	}

	public List<RecordFile> loadSummaryByRecordId(int recordId) {
		List<RecordFile> result = dsl()
			.select(SUMMARY_FIELDS)
			.from(OFC_RECORD_FILE)
			.where(OFC_RECORD_FILE.RECORD_ID.eq(recordId))
			.fetchInto(RecordFile.class);
		return result;
	}
	
	public void insert(RecordFile recordFile) {
		recordFile.setDateCreated(Daos.toTimestamp(new Date()));
		recordFile.setDateModified(Daos.toTimestamp(new Date()));
		super.insert(recordFile);
	}
	
	public void update(RecordFile recordFile) {
		recordFile.setDateModified(Daos.toTimestamp(new Date()));
		super.update(recordFile);
	}

	public void deleteBySurveyId(Integer surveyId) {
		dsl().deleteFrom(OFC_RECORD_FILE)
			.where(OFC_RECORD_FILE.SURVEY_ID.eq(surveyId))
			.execute();
	}
	
	public void deleteByRecordId(Integer recordId) {
		dsl().deleteFrom(OFC_RECORD_FILE)
			.where(OFC_RECORD_FILE.RECORD_ID.eq(recordId))
			.execute();
	}
	
	public void deleteByUuid(String uuid) {
		dsl().deleteFrom(OFC_RECORD_FILE)
			.where(OFC_RECORD_FILE.UUID.eq(uuid))
			.execute();
	}
	
	private DSLContext dsl() {
		return DSL.using(configuration());
	}
}
