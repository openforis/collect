package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_CODE_LIST_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcCodeList.OFC_CODE_LIST;

import java.sql.Connection;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.Survey;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class CodeListItemDao extends MappingJooqDaoSupport<PersistedCodeListItem, CodeListItemDao.JooqFactory> {
	
	@SuppressWarnings("rawtypes")
	private static final TableField[] LABEL_FIELDS = {OFC_CODE_LIST.LABEL1, OFC_CODE_LIST.LABEL2, OFC_CODE_LIST.LABEL3}; 
	@SuppressWarnings("rawtypes")
	private static final TableField[] DESCRIPTION_FIELDS = {OFC_CODE_LIST.DESCRIPTION1, OFC_CODE_LIST.DESCRIPTION2, OFC_CODE_LIST.DESCRIPTION3}; 
	
	public CodeListItemDao() {
		super(CodeListItemDao.JooqFactory.class);
	}

	@Override
	public PersistedCodeListItem loadById(int id) {
		return super.loadById(id);
	}

	@Override
	public void insert(PersistedCodeListItem item) {
		JooqFactory jf = getMappingJooqFactory(item.getCodeList());
		jf.insertQuery(item).execute();
	}

	@Override
	public void update(PersistedCodeListItem item) {
		JooqFactory jf = getMappingJooqFactory(item.getCodeList());
		jf.updateQuery(item).execute();
	}

	public void delete(int id) {
		JooqFactory jf = getMappingJooqFactory(null);
		jf.deleteQuery(id).execute();
	}

	public void deleteBySurvey(int surveyId) {
		deleteBySurvey(surveyId, false);
	}
	
	public void deleteBySurveyWork(int surveyId) {
		deleteBySurvey(surveyId, true);
	}
	
	public void deleteBySurvey(int surveyId, boolean work) {
		JooqFactory jf = getMappingJooqFactory(null);
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(work);
		jf.delete(OFC_CODE_LIST)
			.where(surveyIdField.equal(surveyId))
			.execute();
	}

	public void moveToPublishedSurvey(int publishedSurveyId, int surveyWorkId) {
		JooqFactory jf = getMappingJooqFactory(null);
		jf.update(OFC_CODE_LIST)
			.set(OFC_CODE_LIST.SURVEY_ID, publishedSurveyId)
			.set(OFC_CODE_LIST.SURVEY_WORK_ID, (Integer) null)
			.where(OFC_CODE_LIST.SURVEY_WORK_ID.equal(surveyWorkId))
			.execute();
	}

	public List<PersistedCodeListItem> loadRootItems(CodeList codeList) {
		return loadItems(codeList, (Integer) null);
	}
	
	public PersistedCodeListItem loadRootItem(CodeList codeList, String code) {
		return loadItem(codeList, (Integer) null, code);
	}
	
	public List<PersistedCodeListItem> loadItems(CodeList codeList, Integer parentItemId) {
		JooqFactory jf = getMappingJooqFactory(codeList);
		SelectQuery q = createSelectQuery(jf, codeList, parentItemId);
		Result<Record> result = q.fetch();
		return jf.fromResult(result);
	}
	
	public PersistedCodeListItem loadItem(CodeList codeList, Integer parentItemId, String code) {
		JooqFactory jf = getMappingJooqFactory(codeList);
		SelectQuery q = createSelectQuery(jf, codeList, parentItemId);
		q.addConditions(
				OFC_CODE_LIST.CODE.equal(code)
		);
		Record r = q.fetchOne();
		if ( r == null ) {
			return null;
		} else {
			return jf.fromRecord(r);
		}
	}

	protected SelectQuery createSelectQuery(JooqFactory jf, CodeList codeList, Integer parentItemId) {
		CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
		SelectQuery q = jf.selectQuery();	
		q.addFrom(OFC_CODE_LIST);
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(survey.isWork());
		q.addConditions(
				surveyIdField.equal(survey.getId()),
				OFC_CODE_LIST.CODE_LIST_ID.equal(codeList.getId()),
				OFC_CODE_LIST.PARENT_ID.equal(parentItemId));
		return q;
	}
	
	@Override
	protected JooqFactory getMappingJooqFactory() {
		throw new UnsupportedOperationException();
	}
	
	protected JooqFactory getMappingJooqFactory(CodeList codeList) {
		Connection connection = getConnection();
		return new JooqFactory(connection, codeList);
	}
	
	@Override
	protected DialectAwareJooqFactory getJooqFactory() {
		throw new UnsupportedOperationException();
	}
	
	protected DialectAwareJooqFactory getJooqFactory(CodeList codeList) {
		Connection connection = getConnection();
		return new JooqFactory(connection, codeList);
	}
	
	protected TableField<OfcCodeListRecord, Integer> getSurveyIdField(
			boolean work) {
		TableField<OfcCodeListRecord, Integer> surveyIdField = work ? 
				OFC_CODE_LIST.SURVEY_WORK_ID: OFC_CODE_LIST.SURVEY_ID;
		return surveyIdField;
	}
	
	protected static class JooqFactory extends MappingJooqFactory<PersistedCodeListItem> {

		private static final long serialVersionUID = 1L;
		
		private CodeList codeList;
		
		public JooqFactory(Connection connection, CodeList codeList) {
			super(connection, OFC_CODE_LIST.ID, OFC_CODE_LIST_ID_SEQ, PersistedCodeListItem.class);
			this.codeList = codeList;
		}
		
		@Override
		protected PersistedCodeListItem newEntity() {
			throw new UnsupportedOperationException();
		}
		
		protected PersistedCodeListItem newEntity(int itemId) {
			return new PersistedCodeListItem(codeList, itemId);
		}
		
		@Override
		public PersistedCodeListItem fromRecord(Record record) {
			int itemId = record.getValue(OFC_CODE_LIST.ITEM_ID);
			PersistedCodeListItem entity = newEntity(itemId);
			fromRecord(record, entity);
			return entity;
		}

		@Override
		public void fromRecord(Record r, PersistedCodeListItem i) {
			i.setSystemId(r.getValue(OFC_CODE_LIST.ID));
			i.setCode(r.getValue(OFC_CODE_LIST.CODE));
			i.setParentId(r.getValue(OFC_CODE_LIST.PARENT_ID));
			extractLabels(r, i);
			extractDescriptions(r, i);
		}

		protected void extractLabels(Record r, PersistedCodeListItem item) {
			Survey survey = codeList.getSurvey();
			item.removeAllLabels();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < languages.size(); i++) {
				String lang = languages.get(i);
				String label = r.getValueAsString(LABEL_FIELDS[i]);
				item.setLabel(lang, label);
			}
		}
		
		protected void extractDescriptions(Record r, PersistedCodeListItem item) {
			Survey survey = codeList.getSurvey();
			item.removeAllDescriptions();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < languages.size(); i++) {
				String lang = languages.get(i);
				String label = r.getValueAsString(DESCRIPTION_FIELDS[i]);
				item.setDescription(lang, label);
			}
		}

		@Override
		public void fromObject(PersistedCodeListItem item, StoreQuery<?> q) {
			q.addValue(OFC_CODE_LIST.ID, item.getSystemId());
			CollectSurvey survey = (CollectSurvey) item.getSurvey();
			Integer surveyId = survey.getId();
			if ( survey.isWork() ) {
				q.addValue(OFC_CODE_LIST.SURVEY_ID, (Integer) null);
				q.addValue(OFC_CODE_LIST.SURVEY_WORK_ID, surveyId);
			} else {
				q.addValue(OFC_CODE_LIST.SURVEY_ID, surveyId);
				q.addValue(OFC_CODE_LIST.SURVEY_WORK_ID, (Integer) null);
			}
			q.addValue(OFC_CODE_LIST.CODE_LIST_ID, item.getCodeList().getId());
			q.addValue(OFC_CODE_LIST.ITEM_ID, item.getId());
			q.addValue(OFC_CODE_LIST.PARENT_ID, item.getParentId());
			q.addValue(OFC_CODE_LIST.CODE, item.getCode());
			addLabelValues(q, item);
			addDescriptionValues(q, item);
		}

		protected void addLabelValues(StoreQuery<?> q, PersistedCodeListItem item) {
			Survey survey = item.getSurvey();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < LABEL_FIELDS.length; i++) {
				@SuppressWarnings("unchecked")
				TableField<?, String> field = LABEL_FIELDS[i];
				String label;
				if ( i < languages.size() ) {
					String lang = languages.get(i);
					label = getLabel(item, lang);
				} else {
					label = null;
				}
				q.addValue(field, label);
			}
		}

		protected String getLabel(PersistedCodeListItem item, String lang) {
			String label = item.getLabel(lang);
			if ( label == null ) {
				CollectSurvey survey = (CollectSurvey) item.getSurvey();
				if ( survey.isDefaultLanguage(lang) ) {
					label = item.getLabel(null);
				}
			}
			return label;
		}
		
		protected void addDescriptionValues(StoreQuery<?> q, PersistedCodeListItem item) {
			Survey survey = item.getSurvey();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < DESCRIPTION_FIELDS.length; i++) {
				@SuppressWarnings("unchecked")
				TableField<?, String> field = DESCRIPTION_FIELDS[i];
				String description;
				if ( i < languages.size() ) {
					String lang = languages.get(i);
					description = getDescription(item, lang);
				} else {
					description = null;
				}
				q.addValue(field, description);
			}
		}

		protected String getDescription(PersistedCodeListItem item, String lang) {
			String description = item.getDescription(lang);
			if ( description == null ) {
				CollectSurvey survey = (CollectSurvey) item.getSurvey();
				if ( survey.isDefaultLanguage(lang) ) {
					description = item.getDescription(null);
				}
			}
			return description;
		}
		
		@Override
		protected void setId(PersistedCodeListItem t, int id) {
			t.setSystemId(id);
		}

		@Override
		protected Integer getId(PersistedCodeListItem t) {
			return t.getSystemId();
		}
		
	}

}
