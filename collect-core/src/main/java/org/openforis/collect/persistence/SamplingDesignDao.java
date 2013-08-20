package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SAMPLING_DESIGN_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcSamplingDesign.OFC_SAMPLING_DESIGN;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.BatchBindStep;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.jooq.impl.Factory;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.collect.persistence.jooq.tables.records.OfcSamplingDesignRecord;
import org.openforis.idm.model.Coordinate;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class SamplingDesignDao extends MappingJooqDaoSupport<SamplingDesignItem, SamplingDesignDao.JooqFactory> {
	
	@SuppressWarnings("rawtypes")
	private static final TableField[] LEVEL_CODE_FIELDS = {OFC_SAMPLING_DESIGN.LEVEL1, OFC_SAMPLING_DESIGN.LEVEL2, OFC_SAMPLING_DESIGN.LEVEL3}; 
	@SuppressWarnings("rawtypes")
	private static final TableField[] FIELDS = {
		OFC_SAMPLING_DESIGN.ID,
		OFC_SAMPLING_DESIGN.SURVEY_ID,
		OFC_SAMPLING_DESIGN.SURVEY_WORK_ID,
		OFC_SAMPLING_DESIGN.LOCATION,
		OFC_SAMPLING_DESIGN.LEVEL1,
		OFC_SAMPLING_DESIGN.LEVEL2,
		OFC_SAMPLING_DESIGN.LEVEL3
	};
	
	public SamplingDesignDao() {
		super(SamplingDesignDao.JooqFactory.class);
	}

	@Override
	public SamplingDesignItem loadById(int id) {
		return super.loadById(id);
	}

	@Override
	public void insert(SamplingDesignItem entity) {
		super.insert(entity);
	}

	@Override
	public void update(SamplingDesignItem entity) {
		super.update(entity);
	}

	@Override
	public void delete(int id) {
		super.delete(id);
	}

	public int countPerSurvey(int surveyId) {
		return count(false, surveyId);
	}
	
	public int countPerSurveyWork(int surveyWorkId) {
		return count(true, surveyWorkId);
	}
	
	public int count(boolean work, int surveyId) {
		JooqFactory f = getMappingJooqFactory();
		SelectQuery q = f.selectCountQuery();
		TableField<OfcSamplingDesignRecord, Integer> surveyIdField = getSurveyIdField(work);
		q.addConditions(surveyIdField.equal(surveyId));
		Record r = q.fetchOne();
		return (Integer) r.getValue(0);
	}
	
	public void deleteBySurvey(int surveyId) {
		deleteBySurvey(false, surveyId);
	}
	
	public void deleteBySurveyWork(int surveyId) {
		deleteBySurvey(true, surveyId);
	}
	
	public void deleteBySurvey(boolean work, int surveyId) {
		JooqFactory jf = getMappingJooqFactory();
		TableField<OfcSamplingDesignRecord, Integer> surveyIdField = getSurveyIdField(work);
		jf.delete(OFC_SAMPLING_DESIGN)
			.where(surveyIdField.equal(surveyId))
			.execute();
	}

	protected TableField<OfcSamplingDesignRecord, Integer> getSurveyIdField(
			boolean work) {
		return work ? OFC_SAMPLING_DESIGN.SURVEY_WORK_ID: OFC_SAMPLING_DESIGN.SURVEY_ID;
	}
	
	public List<SamplingDesignItem> loadItemsBySurvey(int surveyId, int offset, int maxRecords) {
		return loadItems(false, surveyId, offset, maxRecords);
	}
	
	public List<SamplingDesignItem> loadItemsBySurveyWork(int surveyId, int offset, int maxRecords) {
		return loadItems(true, surveyId, offset, maxRecords);
	}
	
	@SuppressWarnings("rawtypes")
	protected List<SamplingDesignItem> loadItems(boolean work, int surveyId, int offset, int maxRecords) {
		JooqFactory jf = getMappingJooqFactory();
		SelectQuery q = jf.selectQuery();	
		q.addFrom(OFC_SAMPLING_DESIGN);
		TableField<OfcSamplingDesignRecord, Integer> surveyIdField = getSurveyIdField(work);
		q.addConditions(surveyIdField.equal(surveyId));
		for (TableField field : LEVEL_CODE_FIELDS) {
			q.addOrderBy(field);
		}
		//add limit
		q.addLimit(offset, maxRecords);
		
		//fetch results
		Result<Record> result = q.fetch();
		
		return jf.fromResult(result);
	}
	
	/**
	 * Inserts the items in batch.
	 * 
	 * @param taxa
	 */
	public void insert(List<SamplingDesignItem> items) {
		if ( items != null && ! items.isEmpty() ) {
			JooqFactory jf = getMappingJooqFactory();
			int id = jf.nextId(OFC_SAMPLING_DESIGN.ID, OFC_SAMPLING_DESIGN_ID_SEQ);
			int maxId = id;
			Insert<OfcSamplingDesignRecord> query = jf.createInsertStatement();
			BatchBindStep batch = jf.batch(query);
			for (SamplingDesignItem item : items) {
				if ( item.getId() == null ) {
					item.setId(id++);
				}
				Object[] values = jf.extractValues(item);
				batch.bind(values);
				maxId = Math.max(maxId, item.getId());
			}
			batch.execute();
			jf.restartSequence(OFC_SAMPLING_DESIGN_ID_SEQ, maxId + 1);
		}
	}
	
	public void duplicateItems(int oldSurveyId, boolean oldSurveyWork, int newSurveyId, boolean newSurveyWork) {
		JooqFactory jf = getMappingJooqFactory();
		int minId = loadMinId(jf, oldSurveyId, oldSurveyWork);
		int nextId = jf.nextId(OFC_SAMPLING_DESIGN.ID, OFC_SAMPLING_DESIGN_ID_SEQ);
		int idGap = nextId - minId;
		Integer selectSurveyIdValue = newSurveyWork ? null: newSurveyId;
		Integer selectSurveyWorkIdValue = newSurveyWork ? newSurveyId: null;
		Field<?>[] selectFields = {
				OFC_SAMPLING_DESIGN.ID.add(idGap),
				Factory.val(selectSurveyIdValue, OFC_SAMPLING_DESIGN.SURVEY_ID),
				Factory.val(selectSurveyWorkIdValue, OFC_SAMPLING_DESIGN.SURVEY_WORK_ID),
				OFC_SAMPLING_DESIGN.LOCATION
			};
		selectFields = ArrayUtils.addAll(selectFields, LEVEL_CODE_FIELDS);
		TableField<OfcSamplingDesignRecord, Integer> oldSurveyIdField = getSurveyIdField(oldSurveyWork);
		Select<?> select = jf.select(selectFields)
			.from(OFC_SAMPLING_DESIGN)
			.where(oldSurveyIdField.equal(oldSurveyId))
			.orderBy(OFC_SAMPLING_DESIGN.ID);
		TableField<?, ?>[] insertFields = FIELDS;
		Insert<OfcSamplingDesignRecord> insert = jf.insertInto(OFC_SAMPLING_DESIGN, insertFields).select(select);
		int insertedCount = insert.execute();
		nextId = nextId + insertedCount;
		jf.restartSequence(OFC_SAMPLING_DESIGN_ID_SEQ, nextId);
	}
	
	protected int loadMinId(JooqFactory jf, int surveyId, boolean work) {
		TableField<OfcSamplingDesignRecord, Integer> surveyIdField = getSurveyIdField(work);
		Integer minId = jf.select(Factory.min(OFC_SAMPLING_DESIGN.ID))
				.from(OFC_SAMPLING_DESIGN)
				.where(surveyIdField.equal(surveyId))
				.fetchOne(0, Integer.class);
		return minId == null ? 0: minId.intValue();
	}

	public void moveItemsToPublishedSurvey(int surveyWorkId, int publishedSurveyId) {
		JooqFactory jf = getMappingJooqFactory();
		jf.update(OFC_SAMPLING_DESIGN)
			.set(OFC_SAMPLING_DESIGN.SURVEY_ID, publishedSurveyId)
			.set(OFC_SAMPLING_DESIGN.SURVEY_WORK_ID, (Integer) null)
			.where(OFC_SAMPLING_DESIGN.SURVEY_WORK_ID.equal(surveyWorkId))
			.execute();
	}

	protected static class JooqFactory extends MappingJooqFactory<SamplingDesignItem> {

		private static final String LOCATION_FORMAT = "#";

		private static final long serialVersionUID = 1L;
		
		private static final String LOCATION_FORMAT_PATTERN = "SRID={0};POINT({1} {2})";
		
		public JooqFactory(Connection connection) {
			super(connection, OFC_SAMPLING_DESIGN.ID, OFC_SAMPLING_DESIGN_ID_SEQ, SamplingDesignItem.class);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void fromRecord(Record r, SamplingDesignItem s) {
			s.setId(r.getValue(OFC_SAMPLING_DESIGN.ID));
			s.setSurveyId(r.getValue(OFC_SAMPLING_DESIGN.SURVEY_ID));
			s.setSurveyWorkId(r.getValue(OFC_SAMPLING_DESIGN.SURVEY_WORK_ID));
			String locationValue = r.getValue(OFC_SAMPLING_DESIGN.LOCATION);
			Coordinate coordinate = Coordinate.parseCoordinate(locationValue);
			s.setSrsId(coordinate == null ? null : coordinate.getSrsId());
			s.setX(coordinate == null ? null : coordinate.getX());
			s.setY(coordinate == null ? null : coordinate.getY());
			for (Field<String> field : LEVEL_CODE_FIELDS) {
				String value = r.getValue(field);
				if ( StringUtils.isNotBlank(value) ) {
					s.addLevelCode(r.getValue(field));
				} else {
					break;
				}
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void fromObject(SamplingDesignItem s, StoreQuery<?> q) {
			q.addValue(OFC_SAMPLING_DESIGN.ID, s.getId());
			q.addValue(OFC_SAMPLING_DESIGN.SURVEY_ID, s.getSurveyId());
			q.addValue(OFC_SAMPLING_DESIGN.SURVEY_WORK_ID, s.getSurveyWorkId());
			q.addValue(OFC_SAMPLING_DESIGN.LOCATION, extractLocation(s));
			String[] levelCodeValues = extractLevelCodeValues(s);
			for ( int i = 0; i < LEVEL_CODE_FIELDS.length; i++ ) {
				Field<String> field = LEVEL_CODE_FIELDS[i];
				String value = levelCodeValues[i];
				q.addValue(field, value);
			}
		}

		protected Insert<OfcSamplingDesignRecord> createInsertStatement() {
			Object[] valuesPlaceholders = new String[FIELDS.length];
			Arrays.fill(valuesPlaceholders, "?");
			return insertInto(OFC_SAMPLING_DESIGN, FIELDS).values(valuesPlaceholders);
		}
		
		protected Object[] extractValues(SamplingDesignItem item) {
			Object[] values = {
					item.getId(), 
					item.getSurveyId(),
					item.getSurveyWorkId(),
					extractLocation(item)
					};
			Object[] levelCodeValues = extractLevelCodeValues(item);
			values = ArrayUtils.addAll(values, levelCodeValues);
			return values;
		}

		protected String[] extractLevelCodeValues(SamplingDesignItem item) {
			List<String> levelCodes = item.getLevelCodes();
			if ( levelCodes.size() > LEVEL_CODE_FIELDS.length ) {
				throw new IllegalArgumentException("Only " + LEVEL_CODE_FIELDS.length + " code level are supported");
			}
			String[] result = new String[LEVEL_CODE_FIELDS.length];
			for ( int i = 0; i < LEVEL_CODE_FIELDS.length; i++ ) {
				String value = i < levelCodes.size() ? levelCodes.get(i): null;
				result[i] = value;
			}
			return result;
		}

		@Override
		protected void setId(SamplingDesignItem t, int id) {
			t.setId(id);
		}

		@Override
		protected Integer getId(SamplingDesignItem t) {
			return t.getId();
		}
		
		public String extractLocation(SamplingDesignItem i) {
			if ( i.getSrsId() == null || i.getX() == null || i.getY() == null ) {
				return null;
			} else {
				DecimalFormat formatter = new DecimalFormat(LOCATION_FORMAT);
				String result = MessageFormat.format(LOCATION_FORMAT_PATTERN, 
						i.getSrsId(), 
						formatter.format(i.getX()),
						formatter.format(i.getY())
						);
				return result;
			}
		}
		
	}

}
