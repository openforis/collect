package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SAMPLING_DESIGN_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcSamplingDesing.OFC_SAMPLING_DESING;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.collect.persistence.jooq.tables.records.OfcSamplingDesingRecord;
import org.openforis.idm.model.Coordinate;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class SamplingDesignDao extends MappingJooqDaoSupport<SamplingDesignItem, SamplingDesignDao.JooqFactory> {
	
	@SuppressWarnings("rawtypes")
	private static final TableField[] LEVEL_CODE_FIELDS = {OFC_SAMPLING_DESING.LEVEL1, OFC_SAMPLING_DESING.LEVEL2, OFC_SAMPLING_DESING.LEVEL3}; 
	
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
		TableField<OfcSamplingDesingRecord, Integer> surveyIdField = work ? OFC_SAMPLING_DESING.SURVEY_WORK_ID:
			OFC_SAMPLING_DESING.SURVEY_ID;
		q.addConditions(surveyIdField.equal(surveyId));
		Record r = q.fetchOne();
		return r.getValueAsInteger(0);
	}
	
	public void deleteBySurvey(int surveyId) {
		deleteBySurvey(false, surveyId);
	}
	
	public void deleteBySurveyWork(int surveyId) {
		deleteBySurvey(true, surveyId);
	}
	
	public void deleteBySurvey(boolean work, int surveyId) {
		JooqFactory jf = getMappingJooqFactory();
		TableField<OfcSamplingDesingRecord, Integer> surveyIdField = work ? OFC_SAMPLING_DESING.SURVEY_WORK_ID: OFC_SAMPLING_DESING.SURVEY_ID;
		jf.delete(OFC_SAMPLING_DESING)
			.where(surveyIdField.equal(surveyId))
			.execute();
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
		q.addFrom(OFC_SAMPLING_DESING);
		TableField<OfcSamplingDesingRecord, Integer> surveyIdField = work ? OFC_SAMPLING_DESING.SURVEY_WORK_ID: OFC_SAMPLING_DESING.SURVEY_ID;
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
	
	protected static class JooqFactory extends MappingJooqFactory<SamplingDesignItem> {

		private static final String LOCATION_FORMAT = "#";

		private static final long serialVersionUID = 1L;
		
		private static final String LOCATION_FORMAT_PATTERN = "SRID={0};POINT({1} {2})";
		
		public JooqFactory(Connection connection) {
			super(connection, OFC_SAMPLING_DESING.ID, OFC_SAMPLING_DESIGN_ID_SEQ, SamplingDesignItem.class);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void fromRecord(Record r, SamplingDesignItem s) {
			s.setId(r.getValue(OFC_SAMPLING_DESING.ID));
			s.setSurveyId(r.getValue(OFC_SAMPLING_DESING.SURVEY_ID));
			s.setSurveyWorkId(r.getValue(OFC_SAMPLING_DESING.SURVEY_WORK_ID));
			String locationValue = r.getValue(OFC_SAMPLING_DESING.LOCATION);
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
			q.addValue(OFC_SAMPLING_DESING.ID, s.getId());
			q.addValue(OFC_SAMPLING_DESING.SURVEY_ID, s.getSurveyId());
			q.addValue(OFC_SAMPLING_DESING.SURVEY_WORK_ID, s.getSurveyWorkId());
			q.addValue(OFC_SAMPLING_DESING.LOCATION, extractLocation(s));
			List<String> levelCodes = s.getLevelCodes();
			int levelsSize = levelCodes.size();
			int maxLevelsSize = LEVEL_CODE_FIELDS.length;
			if ( levelsSize > maxLevelsSize ) {
				throw new IllegalArgumentException("Only " + LEVEL_CODE_FIELDS.length + " code level are supported");
			} else {
				for ( int i = 0; i < LEVEL_CODE_FIELDS.length; i++ ) {
					Field<String> field = LEVEL_CODE_FIELDS[i];
					String value = i >= levelsSize ? null: levelCodes.get(i);
					q.addValue(field, value);
				}
			}
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

