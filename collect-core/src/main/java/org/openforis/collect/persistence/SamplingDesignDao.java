package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SAMPLING_DESIGN_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcSamplingDesign.OFC_SAMPLING_DESIGN;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.BatchBindStep;
import org.jooq.Configuration;
import org.jooq.Cursor;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.persistence.jooq.MappingDSLContext;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcSamplingDesignRecord;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Visitor;
import org.openforis.idm.model.Coordinate;

/**
 * @author S. Ricci
 */
public class SamplingDesignDao extends MappingJooqDaoSupport<Integer, SamplingDesignItem, SamplingDesignDao.SamplingDesignDSLContext> {
	
	@SuppressWarnings("rawtypes")
	private static final TableField[] BASE_FIELDS = {
			OFC_SAMPLING_DESIGN.ID,
			OFC_SAMPLING_DESIGN.SURVEY_ID,
			OFC_SAMPLING_DESIGN.LOCATION
	};

	@SuppressWarnings("rawtypes")
	public static final TableField[] LEVEL_CODE_FIELDS = {
		OFC_SAMPLING_DESIGN.LEVEL1, 
		OFC_SAMPLING_DESIGN.LEVEL2, 
		OFC_SAMPLING_DESIGN.LEVEL3
	};

	@SuppressWarnings("rawtypes")
	public static final TableField[] INFO_FIELDS = {
		OFC_SAMPLING_DESIGN.INFO1, 
		OFC_SAMPLING_DESIGN.INFO2, 
		OFC_SAMPLING_DESIGN.INFO3,
		OFC_SAMPLING_DESIGN.INFO4,
		OFC_SAMPLING_DESIGN.INFO5,
		OFC_SAMPLING_DESIGN.INFO6,
		OFC_SAMPLING_DESIGN.INFO7,
		OFC_SAMPLING_DESIGN.INFO8,
		OFC_SAMPLING_DESIGN.INFO9,
		OFC_SAMPLING_DESIGN.INFO10,
		OFC_SAMPLING_DESIGN.INFO11,
		OFC_SAMPLING_DESIGN.INFO12,
		OFC_SAMPLING_DESIGN.INFO13,
		OFC_SAMPLING_DESIGN.INFO14,
		OFC_SAMPLING_DESIGN.INFO15,
		OFC_SAMPLING_DESIGN.INFO16,
		OFC_SAMPLING_DESIGN.INFO17,
		OFC_SAMPLING_DESIGN.INFO18,
		OFC_SAMPLING_DESIGN.INFO19,
		OFC_SAMPLING_DESIGN.INFO20,
		OFC_SAMPLING_DESIGN.INFO21,
		OFC_SAMPLING_DESIGN.INFO22,
		OFC_SAMPLING_DESIGN.INFO23,
		OFC_SAMPLING_DESIGN.INFO24,
		OFC_SAMPLING_DESIGN.INFO25,
		OFC_SAMPLING_DESIGN.INFO26,
		OFC_SAMPLING_DESIGN.INFO27,
		OFC_SAMPLING_DESIGN.INFO28,
		OFC_SAMPLING_DESIGN.INFO29,
		OFC_SAMPLING_DESIGN.INFO30
	}; 
	
	@SuppressWarnings("rawtypes")
	private static final TableField[] FIELDS = org.openforis.commons.collection.ArrayUtils.join(TableField.class, 
			BASE_FIELDS, LEVEL_CODE_FIELDS, INFO_FIELDS);

	public SamplingDesignDao() {
		super(SamplingDesignDao.SamplingDesignDSLContext.class);
	}

	public int countBySurvey(int surveyId) {
		SelectQuery<?> q = dsl().selectCountQuery();
		q.addConditions(OFC_SAMPLING_DESIGN.SURVEY_ID.equal(surveyId));
		Record r = q.fetchOne();
		return (Integer) r.getValue(0);
	}
	
	public void deleteBySurvey(int surveyId) {
		dsl().delete(OFC_SAMPLING_DESIGN)
			.where(OFC_SAMPLING_DESIGN.SURVEY_ID.equal(surveyId))
			.execute();
	}
	
	public void visitItems(int surveyId, Integer upToLevel, Visitor<SamplingDesignItem> visitor) {
		SamplingDesignDSLContext dsl = dsl();
		
		SelectQuery<Record> q = createSelectItemsQuery(dsl, surveyId, upToLevel);
		
		Cursor<Record> cursor = null;
		try {
			cursor = q.fetchLazy();
			while (cursor.hasNext()) {
				Record record = cursor.fetchOne();
				SamplingDesignItem s = dsl.fromRecord(record);
				visitor.visit(s);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
		    }
		}
	}

	public List<SamplingDesignItem> loadItems(int surveyId, int offset, int maxRecords) {
		return loadItems(surveyId, null, offset, maxRecords);
	}

	public List<SamplingDesignItem> loadItems(int surveyId, Integer upToLevel, int offset, int maxRecords) {
		SamplingDesignDSLContext dsl = dsl();
		SelectQuery<Record> q = createSelectItemsQuery(dsl, surveyId, upToLevel);
		//add limit
		q.addLimit(offset, maxRecords);
		
		//fetch results
		Result<Record> result = q.fetch();
		
		return dsl.fromResult(result);
	}

	private SelectQuery<Record> createSelectItemsQuery(SamplingDesignDSLContext dsl, int surveyId, Integer upToLevel) {
		SelectQuery<Record> q = dsl.selectQuery();	
		q.addFrom(OFC_SAMPLING_DESIGN);
		q.addConditions(OFC_SAMPLING_DESIGN.SURVEY_ID.equal(surveyId));
		
		addLevelKeyNullConditions(q, upToLevel);
		
		q.addOrderBy(OFC_SAMPLING_DESIGN.ID);
		return q;
	}

	public List<SamplingDesignItem> loadChildItems(int surveyId, List<String> parentKeys) {
		return loadChildItems(surveyId, parentKeys.toArray(new String[parentKeys.size()]));
	}
	
	/**
	 * Return the items in the level next to the one defined by the parent keys with level codes different from null
	 */
	public List<SamplingDesignItem> loadChildItems(int surveyId, String... parentKeys) {
		SamplingDesignDSLContext dsl = dsl();
		SelectQuery<Record> q = dsl.selectQuery();	
		q.addFrom(OFC_SAMPLING_DESIGN);
		q.addConditions(OFC_SAMPLING_DESIGN.SURVEY_ID.equal(surveyId));
		addParentKeysConditions(q, parentKeys);
		int nextLevelIndex = parentKeys == null ? 0: parentKeys.length;
		
		//not null value for the "next" level
		//null values for the code fields in the other levels
		if (nextLevelIndex < LEVEL_CODE_FIELDS.length) {
			q.addConditions(LEVEL_CODE_FIELDS[nextLevelIndex].isNotNull());
			addLevelKeyNullConditions(q, nextLevelIndex + 1);
		}

		q.addOrderBy(OFC_SAMPLING_DESIGN.ID);

		//fetch results
		Result<Record> result = q.fetch();
		
		return dsl.fromResult(result);
	}
	
	public SamplingDesignItem loadItem(int surveyId, String... parentKeys) {
		SamplingDesignDSLContext dsl = dsl();
		SelectQuery<Record> q = dsl.selectQuery();	
		q.addFrom(OFC_SAMPLING_DESIGN);
		q.addConditions(OFC_SAMPLING_DESIGN.SURVEY_ID.equal(surveyId));
		addParentKeysConditions(q, parentKeys);
		int nextLevelIndex = parentKeys == null ? 0: parentKeys.length;
		
		addLevelKeyNullConditions(q, nextLevelIndex);

		Record r = q.fetchAny();
		return r == null ? null : dsl.fromRecord(r);
	}

	private void addParentKeysConditions(SelectQuery<Record> q, String... parentKeys) {
		if (parentKeys != null) {
			for (int levelIdx = 0; levelIdx < parentKeys.length; levelIdx ++) {
				@SuppressWarnings("unchecked")
				Field<String> tableField = LEVEL_CODE_FIELDS[levelIdx];
				q.addConditions(tableField.eq(parentKeys[levelIdx]));
			}
		}
	}
	
	/**
	 * Inserts the items in batch.
	 * 
	 * @param taxa
	 */
	public void insert(List<SamplingDesignItem> items) {
		if ( items != null && ! items.isEmpty() ) {
			SamplingDesignDSLContext dsl = dsl();
			int id = dsl.nextId(OFC_SAMPLING_DESIGN.ID, OFC_SAMPLING_DESIGN_ID_SEQ);
			int maxId = id;
			Insert<OfcSamplingDesignRecord> query = dsl.createInsertStatement();
			BatchBindStep batch = dsl.batch(query);
			for (SamplingDesignItem item : items) {
				if ( item.getId() == null ) {
					item.setId(id++);
				}
				Object[] values = dsl.extractValues(item);
				batch.bind(values);
				maxId = Math.max(maxId, item.getId());
			}
			batch.execute();
			dsl.restartSequence(OFC_SAMPLING_DESIGN_ID_SEQ, maxId + 1);
		}
	}
	
	public void copyItems(int oldSurveyId, int newSurveyId) {
		SamplingDesignDSLContext dsl = dsl();
		int minId = loadMinId(dsl, oldSurveyId);
		int nextId = dsl.nextId(OFC_SAMPLING_DESIGN.ID, OFC_SAMPLING_DESIGN_ID_SEQ);
		int idGap = nextId - minId;
		Field<?>[] selectFields = {
				OFC_SAMPLING_DESIGN.ID.add(idGap),
				DSL.val(newSurveyId, OFC_SAMPLING_DESIGN.SURVEY_ID),
				OFC_SAMPLING_DESIGN.LOCATION
			};
		selectFields = ArrayUtils.addAll(selectFields, LEVEL_CODE_FIELDS);
		selectFields = ArrayUtils.addAll(selectFields, INFO_FIELDS);
		
		Select<?> select = dsl.select(selectFields)
			.from(OFC_SAMPLING_DESIGN)
			.where(OFC_SAMPLING_DESIGN.SURVEY_ID.equal(oldSurveyId))
			.orderBy(OFC_SAMPLING_DESIGN.ID);
		TableField<?, ?>[] insertFields = FIELDS;
		Insert<OfcSamplingDesignRecord> insert = dsl.insertInto(OFC_SAMPLING_DESIGN, insertFields).select(select);
		int insertedCount = insert.execute();
		nextId = nextId + insertedCount;
		dsl.restartSequence(OFC_SAMPLING_DESIGN_ID_SEQ, nextId);
	}
	
	protected int loadMinId(SamplingDesignDSLContext jf, int surveyId) {
		Integer minId = jf.select(DSL.min(OFC_SAMPLING_DESIGN.ID))
				.from(OFC_SAMPLING_DESIGN)
				.where(OFC_SAMPLING_DESIGN.SURVEY_ID.equal(surveyId))
				.fetchOne(0, Integer.class);
		return minId == null ? 0: minId.intValue();
	}

	public void moveItems(int fromSurveyId, int toSurveyId) {
		dsl().update(OFC_SAMPLING_DESIGN)
			.set(OFC_SAMPLING_DESIGN.SURVEY_ID, toSurveyId)
			.where(OFC_SAMPLING_DESIGN.SURVEY_ID.equal(fromSurveyId))
			.execute();
	}

	private void addLevelKeyNullConditions(SelectQuery<Record> q, Integer fromLevel) {
		if (fromLevel == null || fromLevel == 0) {
			return;
		}
		for (int levelIdx = fromLevel; levelIdx < LEVEL_CODE_FIELDS.length; levelIdx ++) {
			q.addConditions(LEVEL_CODE_FIELDS[levelIdx].isNull());
		}
	}

	protected static class SamplingDesignDSLContext extends MappingDSLContext<Integer, SamplingDesignItem> {

		private static final long serialVersionUID = 1L;

		private static final String LOCATION_POINT_FORMAT = "#.#######";
		private static final String LOCATION_PATTERN = "SRID={0};POINT({1} {2})";

		public SamplingDesignDSLContext(Configuration config) {
			super(config, OFC_SAMPLING_DESIGN.ID, OFC_SAMPLING_DESIGN_ID_SEQ, SamplingDesignItem.class);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void fromRecord(Record r, SamplingDesignItem s) {
			s.setId(r.getValue(OFC_SAMPLING_DESIGN.ID));
			s.setSurveyId(r.getValue(OFC_SAMPLING_DESIGN.SURVEY_ID));
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
			s.setInfoAttributes(extractFields(r, INFO_FIELDS));
		}

		@Override
		@SuppressWarnings("unchecked")
		public void fromObject(SamplingDesignItem s, StoreQuery<?> q) {
			q.addValue(OFC_SAMPLING_DESIGN.ID, s.getId());
			q.addValue(OFC_SAMPLING_DESIGN.SURVEY_ID, s.getSurveyId());
			q.addValue(OFC_SAMPLING_DESIGN.LOCATION, extractLocation(s));
			List<String> levelCodes = s.getLevelCodes();
			addFieldValues(q, LEVEL_CODE_FIELDS, levelCodes);
			addFieldValues(q, INFO_FIELDS, s.getInfoAttributes());
		}

		protected Insert<OfcSamplingDesignRecord> createInsertStatement() {
			Object[] valuesPlaceholders = new String[FIELDS.length];
			Arrays.fill(valuesPlaceholders, "?");
			return insertInto(OFC_SAMPLING_DESIGN, FIELDS).values(valuesPlaceholders);
		}
		
		@SuppressWarnings("unchecked")
		protected Object[] extractValues(SamplingDesignItem item) {
			List<Object> values = new ArrayList<Object>(BASE_FIELDS.length + LEVEL_CODE_FIELDS.length + INFO_FIELDS.length);
			values.addAll(Arrays.asList(
				item.getId(), 
				item.getSurveyId(),
				extractLocation(item)
			));
			//add level codes
			values.addAll(CollectionUtils.copyAndFillWithNulls(item.getLevelCodes(), LEVEL_CODE_FIELDS.length));
			//add info
			values.addAll(CollectionUtils.copyAndFillWithNulls(item.getInfoAttributes(), INFO_FIELDS.length));
			
			return values.toArray(new Object[values.size()]);
		}

		@Override
		protected void setId(SamplingDesignItem t, Integer id) {
			t.setId(id);
		}

		@Override
		protected Integer getId(SamplingDesignItem t) {
			return t.getId();
		}
		
		private String extractLocation(SamplingDesignItem i) {
			if ( i.getSrsId() == null || i.getX() == null || i.getY() == null ) {
				return null;
			} else {
				DecimalFormat formatter = new DecimalFormat(LOCATION_POINT_FORMAT, 
						DecimalFormatSymbols.getInstance(Locale.ENGLISH));
				return MessageFormat.format(LOCATION_PATTERN, 
						i.getSrsId(), 
						formatter.format(i.getX()),
						formatter.format(i.getY())
						);
			}
		}
		
	}

}