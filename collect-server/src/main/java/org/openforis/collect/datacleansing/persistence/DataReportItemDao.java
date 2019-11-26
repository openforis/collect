package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_REPORT_ITEM_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataReportItem.OFC_DATA_REPORT_ITEM;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.jooq.BatchBindStep;
import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.SelectSeekStep1;
import org.jooq.StoreQuery;
import org.jooq.impl.DSL;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryGroup;
import org.openforis.collect.datacleansing.DataReport;
import org.openforis.collect.datacleansing.DataReportItem;
import org.openforis.collect.datacleansing.DataReportItem.Status;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.PersistedObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataReportItemRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataReportItemDao")
@Transactional
public class DataReportItemDao extends MappingJooqDaoSupport<Integer, DataReportItem, DataReportItemDao.JooqDSLContext> {

	private static Field<?>[] FIELDS = new Field<?>[] {
		OFC_DATA_REPORT_ITEM.ID,
		OFC_DATA_REPORT_ITEM.NODE_INDEX,
		OFC_DATA_REPORT_ITEM.PARENT_ENTITY_ID,
		OFC_DATA_REPORT_ITEM.QUERY_ID,
		OFC_DATA_REPORT_ITEM.RECORD_ID,
		OFC_DATA_REPORT_ITEM.REPORT_ID,
		OFC_DATA_REPORT_ITEM.STATUS,
		OFC_DATA_REPORT_ITEM.UUID,
		OFC_DATA_REPORT_ITEM.VALUE
	};
	
	public DataReportItemDao() {
		super(DataReportItemDao.JooqDSLContext.class);
	}

	public List<DataReportItem> loadByReport(DataReport report) {
		return loadByReport(report, null, null);
	}

	public void deleteByReport(DataReport report) {
		dsl().delete(OFC_DATA_REPORT_ITEM)
			.where(OFC_DATA_REPORT_ITEM.REPORT_ID.eq(report.getId()))
			.execute();
	}
	
	public void deleteBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl();
		dsl.delete(OFC_DATA_REPORT_ITEM)
			.where(OFC_DATA_REPORT_ITEM.REPORT_ID.in(
					DataReportDao.createDataReportIdsBySurveyQuery(dsl, survey)
					)
			)
			.execute();
	}

	public int countItems(DataReport report) {
		JooqDSLContext dsl = dsl(report);
		SelectQuery<?> q = dsl.selectCountQuery();
		q.addConditions(OFC_DATA_REPORT_ITEM.REPORT_ID.eq(report.getId()));
		Record record = q.fetchOne();
		return (Integer) record.getValue(0);
	}
	
	public int countAffectedRecords(DataReport report) {
		JooqDSLContext dsl = dsl(report);
		SelectQuery<?> subSelect = dsl.selectQuery();
		subSelect.addSelect(OFC_DATA_REPORT_ITEM.RECORD_ID);
		subSelect.addFrom(OFC_DATA_REPORT_ITEM);
		subSelect.addConditions(OFC_DATA_REPORT_ITEM.REPORT_ID.eq(report.getId()));
		subSelect.addGroupBy(OFC_DATA_REPORT_ITEM.RECORD_ID);
		subSelect.asTable("report_record");
		SelectQuery<?> select = dsl.selectQuery();
		select.addSelect(DSL.count());
		select.addFrom(subSelect);
		Record record = select.fetchOne();
		return (Integer) record.getValue(0);
	}
	
	public List<DataReportItem> loadByReport(DataReport report, Integer offset, Integer limit) {
		JooqDSLContext dsl = dsl(report);
		SelectSeekStep1<OfcDataReportItemRecord, Integer> select = dsl
			.selectFrom(OFC_DATA_REPORT_ITEM)
			.where(OFC_DATA_REPORT_ITEM.REPORT_ID.eq(report.getId()))
			.orderBy(OFC_DATA_REPORT_ITEM.RECORD_ID);
		if (offset != null && limit != null) {
			select.limit(offset, limit);
		}
		Result<OfcDataReportItemRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	public void insert(DataReport report, List<DataReportItem> items) {
		JooqDSLContext dsl = dsl(report);
		int nextId = dsl.nextId();
		int maxId = nextId;
		Insert<OfcDataReportItemRecord> insert = dsl.createInsertStatement();
		BatchBindStep batch = dsl.batch(insert);
		for (DataReportItem item : items) {
			Integer id = item.getId();
			if ( id == null ) {
				id = nextId++;
				item.setId(id);
			}
			Object[] values = dsl.extractValues(item);
			batch.bind(values);
			maxId = Math.max(maxId, id);
		}
		batch.execute();
		dsl.restartSequence(maxId + 1);
	}
	
	@Override
	protected JooqDSLContext dsl() {
		return dsl(null);
	}
	
	protected JooqDSLContext dsl(DataReport report) {
		return new JooqDSLContext(getConfiguration(), report);
	}
	
	protected static class JooqDSLContext extends PersistedObjectMappingDSLContext<Integer, DataReportItem> {

		private static final long serialVersionUID = 1L;
		
		private DataReport report;
		
		public JooqDSLContext(Configuration config, DataReport report) {
			super(config, OFC_DATA_REPORT_ITEM.ID, OFC_DATA_REPORT_ITEM_ID_SEQ, DataReportItem.class);
			this.report = report;
		}
		
		@Override
		protected DataReportItem newEntity() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public DataReportItem fromRecord(Record r) {
			Integer queryId = r.getValue(OFC_DATA_REPORT_ITEM.QUERY_ID);
			DataQueryGroup queryGroup = this.report.getQueryGroup();
			DataQuery dataQuery = queryGroup.getQuery(queryId);
			
			DataReportItem o = new DataReportItem(report, dataQuery);
			o.setId(r.getValue(OFC_DATA_REPORT_ITEM.ID));
			o.setNodeIndex(r.getValue(OFC_DATA_REPORT_ITEM.NODE_INDEX));
			o.setParentEntityId(r.getValue(OFC_DATA_REPORT_ITEM.PARENT_ENTITY_ID));
			o.setRecordId(r.getValue(OFC_DATA_REPORT_ITEM.RECORD_ID));
			o.setStatus(Status.fromCode(r.getValue(OFC_DATA_REPORT_ITEM.STATUS).charAt(0)));
			o.setUuid(UUID.fromString(r.getValue(OFC_DATA_REPORT_ITEM.UUID)));
			o.setValue(r.getValue(OFC_DATA_REPORT_ITEM.VALUE));
			
			return o;
		}
		
		@Override
		protected void fromObject(DataReportItem o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_REPORT_ITEM.NODE_INDEX, o.getNodeIndex());
			q.addValue(OFC_DATA_REPORT_ITEM.PARENT_ENTITY_ID, o.getParentEntityId());
			q.addValue(OFC_DATA_REPORT_ITEM.QUERY_ID, o.getQuery().getId());
			q.addValue(OFC_DATA_REPORT_ITEM.RECORD_ID, o.getRecordId());
			q.addValue(OFC_DATA_REPORT_ITEM.REPORT_ID, o.getReport().getId());
			q.addValue(OFC_DATA_REPORT_ITEM.STATUS, String.valueOf(o.getStatus().getCode()));
			q.addValue(OFC_DATA_REPORT_ITEM.UUID, o.getUuid().toString());
			q.addValue(OFC_DATA_REPORT_ITEM.VALUE, o.getValue());
		}
		
		public Insert<OfcDataReportItemRecord> createInsertStatement() {
			Object[] valuesPlaceholders = new String[FIELDS.length];
			Arrays.fill(valuesPlaceholders, "?");
			return insertInto(OFC_DATA_REPORT_ITEM, FIELDS).values(valuesPlaceholders);
		}
		
		public Object[] extractValues(DataReportItem item) {
			return new Object[] {
				item.getId(),
				item.getNodeIndex(),
				item.getParentEntityId(),
				item.getQuery().getId(),
				item.getRecordId(),
				item.getReport().getId(),
				item.getStatus().getCode(),
				item.getUuid(),
				item.getValue()
			};
		}

	}

}

