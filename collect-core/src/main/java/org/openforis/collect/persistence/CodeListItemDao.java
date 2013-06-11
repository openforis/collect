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
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.Survey;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class CodeListItemDao extends MappingJooqDaoSupport<ExternalCodeListItem, CodeListItemDao.JooqFactory> {
	
	@SuppressWarnings("rawtypes")
	private static final TableField[] LABEL_FIELDS = {OFC_CODE_LIST.LABEL1, OFC_CODE_LIST.LABEL2, OFC_CODE_LIST.LABEL3}; 
	@SuppressWarnings("rawtypes")
	private static final TableField[] DESCRIPTION_FIELDS = {OFC_CODE_LIST.DESCRIPTION1, OFC_CODE_LIST.DESCRIPTION2, OFC_CODE_LIST.DESCRIPTION3}; 
	
	public CodeListItemDao() {
		super(CodeListItemDao.JooqFactory.class);
	}

	@Override
	public ExternalCodeListItem loadById(int id) {
		return super.loadById(id);
	}

	@Override
	public void insert(ExternalCodeListItem item) {
		super.insert(item);
	}

	@Override
	public void update(ExternalCodeListItem item) {
		super.update(item);
	}

	@Override
	public void delete(int id) {
		super.delete(id);
	}

	public void deleteBySurvey(int surveyId) {
		deleteBySurvey(false, surveyId);
	}
	
	public void deleteBySurveyWork(int surveyId) {
		deleteBySurvey(true, surveyId);
	}
	
	public void deleteBySurvey(boolean work, int surveyId) {
		JooqFactory jf = getMappingJooqFactory();
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(work);
		jf.delete(OFC_CODE_LIST)
			.where(surveyIdField.equal(surveyId))
			.execute();
	}

	public List<ExternalCodeListItem> loadRootItems(int surveyId, boolean surveyWork, int codeListId) {
		return loadItems(surveyId, false, codeListId, (Integer) null);
	}
	
	public ExternalCodeListItem loadRootItem(int surveyId, boolean surveyWork, int codeListId, String code) {
		return loadItem(surveyId, surveyWork, codeListId, (Integer) null, code);
	}
	
	public List<ExternalCodeListItem> loadItems(int surveyId, boolean surveyWork, int codeListId, Integer parentItemId) {
		JooqFactory jf = getMappingJooqFactory();
		SelectQuery q = jf.selectQuery();	
		q.addFrom(OFC_CODE_LIST);
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(surveyWork);
		q.addConditions(
				surveyIdField.equal(surveyId),
				OFC_CODE_LIST.CODE_LIST_ID.equal(codeListId),
				OFC_CODE_LIST.PARENT_ID.equal(parentItemId));
		Result<Record> result = q.fetch();
		return jf.fromResult(result);
	}
	
	public ExternalCodeListItem loadItem(int surveyId, boolean surveyWork, int codeListId, Integer parentItemId, String code) {
		JooqFactory jf = getMappingJooqFactory();
		SelectQuery q = jf.selectQuery();	
		q.addFrom(OFC_CODE_LIST);
		TableField<OfcCodeListRecord, Integer> surveyIdField = getSurveyIdField(surveyWork);
		q.addConditions(
				surveyIdField.equal(surveyId),
				OFC_CODE_LIST.CODE_LIST_ID.equal(codeListId),
				OFC_CODE_LIST.PARENT_ID.equal(parentItemId),
				OFC_CODE_LIST.CODE.equal(code));
		Record r = q.fetchOne();
		return jf.fromRecord(r);
	}

	@Override
	protected DialectAwareJooqFactory getJooqFactory() {
		throw new UnsupportedOperationException();
	}
	
	protected DialectAwareJooqFactory getJooqFactory(CodeList codeList, boolean surveyWork) {
		Connection connection = getConnection();
		return new JooqFactory(connection, codeList, surveyWork);
	}
	
	protected TableField<OfcCodeListRecord, Integer> getSurveyIdField(
			boolean work) {
		TableField<OfcCodeListRecord, Integer> surveyIdField = work ? 
				OFC_CODE_LIST.SURVEY_WORK_ID: OFC_CODE_LIST.SURVEY_ID;
		return surveyIdField;
	}
	
	protected static class JooqFactory extends MappingJooqFactory<ExternalCodeListItem> {

		private static final long serialVersionUID = 1L;
		
		private CodeList codeList;
		private boolean surveyWork;
		
		public JooqFactory(Connection connection, CodeList codeList, boolean surveyWork) {
			super(connection, OFC_CODE_LIST.ID, OFC_CODE_LIST_ID_SEQ, ExternalCodeListItem.class);
			this.codeList = codeList;
			this.surveyWork = surveyWork;
		}
		
		@Override
		protected ExternalCodeListItem newEntity() {
			throw new UnsupportedOperationException();
		}
		
		protected ExternalCodeListItem newEntity(int itemId) {
			return new ExternalCodeListItem(codeList, itemId, null);
		}
		
		@Override
		public ExternalCodeListItem fromRecord(Record record) {
			int itemId = record.getValue(OFC_CODE_LIST.ITEM_ID);
			ExternalCodeListItem entity = newEntity(itemId);
			fromRecord(record, entity);
			return entity;
		}

		@Override
		public void fromRecord(Record r, ExternalCodeListItem i) {
			i.setSystemId(r.getValue(OFC_CODE_LIST.ID));
			i.setCode(r.getValue(OFC_CODE_LIST.CODE));
			i.setParentId(r.getValue(OFC_CODE_LIST.PARENT_ID));
			extractLabels(r, i);
			extractDescriptions(r, i);
		}

		protected void extractLabels(Record r, ExternalCodeListItem item) {
			Survey survey = codeList.getSurvey();
			item.removeAllLabels();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < languages.size(); i++) {
				String lang = languages.get(i);
				String label = r.getValueAsString(LABEL_FIELDS[i]);
				item.setLabel(lang, label);
			}
		}
		
		protected void extractDescriptions(Record r, ExternalCodeListItem item) {
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
		public void fromObject(ExternalCodeListItem item, StoreQuery<?> q) {
			q.addValue(OFC_CODE_LIST.ID, item.getSystemId());
			q.addValue(OFC_CODE_LIST.CODE_LIST_ID, item.getCodeList().getId());
			Survey survey = item.getSurvey();
			Integer surveyId = survey.getId();
			if ( surveyWork ) {
				q.addValue(OFC_CODE_LIST.SURVEY_ID, null);
				q.addValue(OFC_CODE_LIST.SURVEY_WORK_ID, surveyId);
			} else {
				q.addValue(OFC_CODE_LIST.SURVEY_ID, surveyId);
				q.addValue(OFC_CODE_LIST.SURVEY_WORK_ID, null);
			}
			addLabelValues(q, item);
			addDescriptionValues(q, item);
		}

		@SuppressWarnings("unchecked")
		protected void addLabelValues(StoreQuery<?> q, ExternalCodeListItem item) {
			Survey survey = item.getSurvey();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < languages.size() && i < LABEL_FIELDS.length; i++) {
				String lang = languages.get(i);
				q.addValue(LABEL_FIELDS[i], lang);
			}
			for (int i = languages.size(); i < LABEL_FIELDS.length; i++ ) {
				q.addValue(LABEL_FIELDS[i], null);
			}
		}

		@SuppressWarnings("unchecked")
		protected void addDescriptionValues(StoreQuery<?> q, ExternalCodeListItem item) {
			Survey survey = item.getSurvey();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < languages.size() && i < DESCRIPTION_FIELDS.length; i++) {
				String lang = languages.get(i);
				q.addValue(DESCRIPTION_FIELDS[i], lang);
			}
			for (int i = languages.size(); i < DESCRIPTION_FIELDS.length; i++ ) {
				q.addValue(DESCRIPTION_FIELDS[i], null);
			}
		}

		@Override
		protected void setId(ExternalCodeListItem t, int id) {
			t.setSystemId(id);
		}

		@Override
		protected Integer getId(ExternalCodeListItem t) {
			return t.getSystemId();
		}
		
	}
}
