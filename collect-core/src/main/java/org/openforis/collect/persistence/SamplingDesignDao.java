package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SAMPLING_DESIGN_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcSamplingDesign.OFC_SAMPLING_DESIGN;

import java.sql.Connection;
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
import org.openforis.idm.model.Coordinate;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class SamplingDesignDao extends MappingJooqDaoSupport<SamplingDesignItem, SamplingDesignDao.SamplingDesignDSLContext> {
	
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
		OFC_SAMPLING_DESIGN.INFO20
	}; 
	
	@SuppressWarnings("rawtypes")
	private static final TableField[] FIELDS = org.openforis.commons.collection.ArrayUtils.join(TableField.class, 
			BASE_FIELDS, LEVEL_CODE_FIELDS, INFO_FIELDS);

	public SamplingDesignDao() {
		super(SamplingDesignDao.SamplingDesignDSLContext.class);
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

	public List<SamplingDesignItem> loadItems(int surveyId, int offset, int maxRecords) {
		SamplingDesignDSLContext dsl = dsl();
		SelectQuery<Record> q = dsl.selectQuery();	
		q.addFrom(OFC_SAMPLING_DESIGN);
		q.addConditions(OFC_SAMPLING_DESIGN.SURVEY_ID.equal(surveyId));
		q.addOrderBy(OFC_SAMPLING_DESIGN.ID);
		//add limit
		q.addLimit(offset, maxRecords);
		
		//fetch results
		Result<Record> result = q.fetch();
		
		return dsl.fromResult(result);
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

	protected static class SamplingDesignDSLContext extends MappingDSLContext<SamplingDesignItem> {

		private static final long serialVersionUID = 1L;

		private static final String LOCATION_POINT_FORMAT = "#.#######";
		private static final String LOCATION_PATTERN = "SRID={0};POINT({1} {2})";

		public SamplingDesignDSLContext(Connection connection) {
			super(connection, OFC_SAMPLING_DESIGN.ID, OFC_SAMPLING_DESIGN_ID_SEQ, SamplingDesignItem.class);
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
			for (int i = 0; i < INFO_FIELDS.length; i++) {
				Field<String> field = INFO_FIELDS[i];
				String value = r.getValue(field);
				s.addInfoAttribute(value);
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void fromObject(SamplingDesignItem s, StoreQuery<?> q) {
			q.addValue(OFC_SAMPLING_DESIGN.ID, s.getId());
			q.addValue(OFC_SAMPLING_DESIGN.SURVEY_ID, s.getSurveyId());
			q.addValue(OFC_SAMPLING_DESIGN.LOCATION, extractLocation(s));
			String[] levelCodeValues = extractLevelCodeValues(s);
			for ( int i = 0; i < levelCodeValues.length; i++ ) {
				String value = levelCodeValues[i];
				Field<String> field = LEVEL_CODE_FIELDS[i];
				q.addValue(field, value);
			}
			List<String> infoValues = s.getInfoAttributes();
			for ( int i = 0; i < infoValues.size(); i++ ) {
				String value = infoValues.get(i);
				Field<String> field = INFO_FIELDS[i];
				q.addValue(field, value);
			}
		}

		protected Insert<OfcSamplingDesignRecord> createInsertStatement() {
			Object[] valuesPlaceholders = new String[FIELDS.length];
			Arrays.fill(valuesPlaceholders, "?");
			return insertInto(OFC_SAMPLING_DESIGN, FIELDS).values(valuesPlaceholders);
		}
		
		@SuppressWarnings("unchecked")
		protected Object[] extractValues(SamplingDesignItem item) {
			List<Object> values = new ArrayList<Object>();
			values.addAll(Arrays.asList(
				item.getId(), 
				item.getSurveyId(),
				extractLocation(item)
			));
			//add level codes
			Object[] levelCodeValues = extractLevelCodeValues(item);
			values.addAll(Arrays.asList(levelCodeValues));
			
			//add info
			Object[] infos = new Object[INFO_FIELDS.length];
			Arrays.fill(infos, null);
			List<String> itemInfos = item.getInfoAttributes();
			for (int i = 0; i < itemInfos.size(); i++) {
				String info = itemInfos.get(i);
				infos[i] = info;
			}
			values.addAll(Arrays.asList(infos));
			Object[] result = values.toArray(new Object[0]);
			return result;
		}

		protected String[] extractLevelCodeValues(SamplingDesignItem item) {
			List<String> levelCodes = item.getLevelCodes();
			if ( levelCodes.size() > LEVEL_CODE_FIELDS.length ) {
				throw new IllegalArgumentException("Only " + LEVEL_CODE_FIELDS.length + " code levels are supported");
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
				DecimalFormat formatter = new DecimalFormat(LOCATION_POINT_FORMAT, 
						DecimalFormatSymbols.getInstance(Locale.ENGLISH));
				String result = MessageFormat.format(LOCATION_PATTERN, 
						i.getSrsId(), 
						formatter.format(i.getX()),
						formatter.format(i.getY())
						);
				return result;
			}
		}
		
	}

}
