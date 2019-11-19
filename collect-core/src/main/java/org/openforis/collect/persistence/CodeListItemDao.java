package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_CODE_LIST_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcCodeList.OFC_CODE_LIST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.jooq.BatchBindStep;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.Cursor;
import org.jooq.DeleteConditionStep;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectQuery;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FileWrapper;
import org.openforis.collect.persistence.jooq.MappingDSLContext;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcCodeListRecord;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Visitor;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyObject;

/**
 * @author S. Ricci
 */
public class CodeListItemDao extends MappingJooqDaoSupport<Long, PersistedCodeListItem, CodeListItemDao.JooqDSLContext> {
	
	@SuppressWarnings("rawtypes")
	private static final TableField[] LABEL_FIELDS = {
		OFC_CODE_LIST.LABEL1, 
		OFC_CODE_LIST.LABEL2, 
		OFC_CODE_LIST.LABEL3,
		OFC_CODE_LIST.LABEL4,
		OFC_CODE_LIST.LABEL5
	}; 
	
	@SuppressWarnings("rawtypes")
	private static final TableField[] DESCRIPTION_FIELDS = {
		OFC_CODE_LIST.DESCRIPTION1, 
		OFC_CODE_LIST.DESCRIPTION2, 
		OFC_CODE_LIST.DESCRIPTION3,
		OFC_CODE_LIST.DESCRIPTION4,
		OFC_CODE_LIST.DESCRIPTION5
	};
	
	@SuppressWarnings("rawtypes")
	private static final TableField[] POJO_FIELDS = {
		OFC_CODE_LIST.ID,
		OFC_CODE_LIST.SURVEY_ID,
		OFC_CODE_LIST.CODE_LIST_ID,
		OFC_CODE_LIST.ITEM_ID,
		OFC_CODE_LIST.PARENT_ID,
		OFC_CODE_LIST.LEVEL,
		OFC_CODE_LIST.SORT_ORDER,
		OFC_CODE_LIST.CODE,
		OFC_CODE_LIST.QUALIFIABLE,
		OFC_CODE_LIST.SINCE_VERSION_ID,
		OFC_CODE_LIST.DEPRECATED_VERSION_ID,
		OFC_CODE_LIST.IMAGE_FILE_NAME,
		OFC_CODE_LIST.COLOR,
		OFC_CODE_LIST.LABEL1, 
		OFC_CODE_LIST.LABEL2, 
		OFC_CODE_LIST.LABEL3,
		OFC_CODE_LIST.LABEL4,
		OFC_CODE_LIST.LABEL5, 
		OFC_CODE_LIST.DESCRIPTION1, 
		OFC_CODE_LIST.DESCRIPTION2, 
		OFC_CODE_LIST.DESCRIPTION3,
		OFC_CODE_LIST.DESCRIPTION4,
		OFC_CODE_LIST.DESCRIPTION5
	};
	
	@SuppressWarnings("rawtypes")
	private static final TableField[] ALL_FIELDS = ArrayUtils.addAll(
		POJO_FIELDS, 
		OFC_CODE_LIST.IMAGE_CONTENT
	);
	
	private boolean useCache;
	private CodeListItemCache cache;
	
	public CodeListItemDao() {
		super(CodeListItemDao.JooqDSLContext.class);
		useCache = false;
		cache = new CodeListItemCache();
	}

	public PersistedCodeListItem loadById(CodeList list, long systemId) {
		JooqDSLContext dsl = dsl(list);
		ResultQuery<?> selectQuery = dsl.selectByIdQuery(systemId);
		Record r = selectQuery.fetchOne();
		if ( r == null ) {
			return null;
		} else {
			return dsl.fromRecord(r);
		}
	}
	
	public FileWrapper loadImageContent(PersistedCodeListItem item) {
		JooqDSLContext dsl = dsl(item.getCodeList());
		ResultQuery<?> selectQuery = dsl.selectByIdQuery(item.getSystemId());
		Record r = selectQuery.fetchOne();
		if ( r == null ) {
			return null;
		} else {
			byte[] content = r.getValue(OFC_CODE_LIST.IMAGE_CONTENT);
			if (content == null) {
				return null;
			} else {
				String fileName = r.getValue(OFC_CODE_LIST.IMAGE_FILE_NAME);
				return new FileWrapper(content, fileName);
			}
		}
	}

	public void saveImageContent(PersistedCodeListItem item, FileWrapper fileWrapper) {
		JooqDSLContext dsl = dsl(item.getCodeList());
		dsl.update(dsl.getTable())
			.set(OFC_CODE_LIST.IMAGE_CONTENT, fileWrapper.getContent())
			.set(OFC_CODE_LIST.IMAGE_FILE_NAME, fileWrapper.getFileName())
			.where(OFC_CODE_LIST.ID.eq(item.getSystemId()))
			.execute();
	}
	
	public void deleteImageContent(PersistedCodeListItem item) {
		JooqDSLContext dsl = dsl(item.getCodeList());
		dsl.update(dsl.getTable())
			.set(OFC_CODE_LIST.IMAGE_CONTENT, (byte[]) null)
			.set(OFC_CODE_LIST.IMAGE_FILE_NAME, (String) null)
			.where(OFC_CODE_LIST.ID.eq(item.getSystemId()))
			.execute();
	}
	
	@Override
	public void insert(PersistedCodeListItem item) {
		JooqDSLContext jf = dsl(item.getCodeList());
		jf.insertQuery(item).execute();
	}
	
	/**
	 * Inserts the items in batch.
	 * 
	 * @param items
	 */
	public void insert(List<PersistedCodeListItem> items) {
		insert(items, true);
	}
	
	public void insert(List<PersistedCodeListItem> items, boolean assignIds) {
		if ( items != null && items.size() > 0 ) {
			PersistedCodeListItem firstItem = items.get(0);
			CodeList list = firstItem.getCodeList();
			JooqDSLContext jf = dsl(list);
			long nextId = assignIds ? jf.nextId() : 0;
			long maxId = nextId;
			Insert<OfcCodeListRecord> query = jf.createInsertStatement();
			BatchBindStep batch = jf.batch(query);
			for (PersistedCodeListItem item : items) {
				if (assignIds && item.getSystemId() == null) {
					item.setSystemId(nextId++);
				}
				List<Object> values = jf.extractValues(item);
				batch.bind(values.toArray(new Object[values.size()]));
				if (assignIds)
					maxId = Math.max(maxId, item.getSystemId());
			}
			try {
				batch.execute();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			if (assignIds) {
				jf.restartSequence(maxId + 1);
			}
		}
	}
	
	public void copyItems(int fromSurveyId, int toSurveyId) {
		JooqDSLContext jf = dsl(null);
		long minId = loadMinId(jf, fromSurveyId);
		long nextId = jf.nextId();
		long idGap = nextId - minId;
		List<Field<?>> selectFields = new ArrayList<Field<?>>(ALL_FIELDS.length);
		selectFields.addAll(Arrays.<Field<?>>asList(
				OFC_CODE_LIST.ID.add(idGap),
				DSL.val(toSurveyId, OFC_CODE_LIST.SURVEY_ID),
				OFC_CODE_LIST.CODE_LIST_ID,
				OFC_CODE_LIST.ITEM_ID,
				OFC_CODE_LIST.PARENT_ID.add(idGap),
				OFC_CODE_LIST.LEVEL,
				OFC_CODE_LIST.SORT_ORDER,
				OFC_CODE_LIST.CODE,
				OFC_CODE_LIST.QUALIFIABLE,
				OFC_CODE_LIST.SINCE_VERSION_ID,
				OFC_CODE_LIST.DEPRECATED_VERSION_ID,
				OFC_CODE_LIST.IMAGE_FILE_NAME,
				OFC_CODE_LIST.COLOR
		));
		selectFields.addAll(Arrays.<Field<?>>asList(LABEL_FIELDS));
		selectFields.addAll(Arrays.<Field<?>>asList(DESCRIPTION_FIELDS));
		selectFields.addAll(Arrays.<Field<?>>asList(OFC_CODE_LIST.IMAGE_CONTENT));
		
		Select<?> select = jf.select(selectFields)
			.from(OFC_CODE_LIST)
			.where(OFC_CODE_LIST.SURVEY_ID.equal(fromSurveyId))
			.orderBy(OFC_CODE_LIST.PARENT_ID, OFC_CODE_LIST.ID);
		Insert<OfcCodeListRecord> insert = jf.insertInto(OFC_CODE_LIST, ALL_FIELDS).select(select);
		insert.execute();
		restartIdSequence(jf);
	}
	
	public void copyItems(CodeList fromCodeList, CodeList toCodeList) {
		JooqDSLContext dsl = dsl(fromCodeList);
		List<PersistedCodeListItem> fromItems = loadAllItems(fromCodeList);
		long minId = findMin(CollectionUtils.<Integer, PersistedCodeListItem>project(fromItems, "systemId"));
		long nextId = dsl.nextId();
		long idGap = nextId - minId;
		List<PersistedCodeListItem> newItems = new ArrayList<PersistedCodeListItem>(fromItems.size());
		for (PersistedCodeListItem source : fromItems) {
			PersistedCodeListItem newItem = new PersistedCodeListItem(toCodeList, source.getLevel());
			newItem.copyProperties(source);
			newItem.setSortOrder(source.getSortOrder());
			newItem.setSystemId(source.getSystemId() + idGap);
			newItem.setParentId(source.getParentId() == null ? null : source.getParentId() + idGap);
			newItems.add(newItem);
			nextId ++;
		}
		insert(newItems, false);
		dsl.restartSequence(nextId);
	}

	private void restartIdSequence(JooqDSLContext jf) {
		long maxId = loadMaxId(jf);
		jf.restartSequence(OFC_CODE_LIST_ID_SEQ, maxId + 1);
	}

	@Override
	public void update(PersistedCodeListItem item) {
		CodeList codeList = item.getCodeList();
		JooqDSLContext jf = dsl(codeList);
		jf.updateQuery(item).execute();
		
		if ( isCacheInUse(codeList) ) {
			cache.clearItemsByCodeList(codeList);
		}
	}
	
	public void shiftItem(PersistedCodeListItem item, int toIndex) {
		CodeList list = item.getCodeList();
		List<PersistedCodeListItem> siblings = loadChildItems(list, item.getParentId()); 
		int newSortOrder;
		int prevItemIdx;
		if ( toIndex >= siblings.size() ) {
			prevItemIdx = siblings.size() - 1;
		} else {
			prevItemIdx = toIndex;
		}
		PersistedCodeListItem previousItem = siblings.get(prevItemIdx);
		newSortOrder = previousItem.getSortOrder();
		updateSortOrder(item, newSortOrder);
	}

	protected void updateSortOrder(PersistedCodeListItem item, int newSortOrder) {
		CodeList list = item.getCodeList();
		CollectSurvey survey = (CollectSurvey) list.getSurvey();
		JooqDSLContext dsl = dsl(list);
		Integer oldSortOrder = item.getSortOrder();
		if ( newSortOrder == oldSortOrder ) {
			return;
		} else {
			//give top sort order to the item
			dsl.update(OFC_CODE_LIST)
				.set(OFC_CODE_LIST.SORT_ORDER, 0)
				.where(OFC_CODE_LIST.ID.equal(item.getSystemId()))
				.execute();
			Condition parentIdCondition = item.getParentId() == null ? 
				OFC_CODE_LIST.PARENT_ID.isNull(): 
				OFC_CODE_LIST.PARENT_ID.equal(item.getParentId());
			if ( newSortOrder > oldSortOrder ) {
				//move backwards previous items
				dsl.update(OFC_CODE_LIST)
					.set(OFC_CODE_LIST.SORT_ORDER, OFC_CODE_LIST.SORT_ORDER.sub(1))
					.where(
						OFC_CODE_LIST.SURVEY_ID.equal(survey.getId()),
						OFC_CODE_LIST.CODE_LIST_ID.equal(list.getId()),
						parentIdCondition,
						OFC_CODE_LIST.SORT_ORDER.greaterThan(oldSortOrder),
						OFC_CODE_LIST.SORT_ORDER.lessOrEqual(newSortOrder)
					).execute();
			} else {
				//move forward next items
				dsl.update(OFC_CODE_LIST)
					.set(OFC_CODE_LIST.SORT_ORDER, OFC_CODE_LIST.SORT_ORDER.add(1))
					.where(
						OFC_CODE_LIST.SURVEY_ID.equal(survey.getId()),
						OFC_CODE_LIST.CODE_LIST_ID.equal(list.getId()),
						parentIdCondition,
						OFC_CODE_LIST.SORT_ORDER.greaterOrEqual(newSortOrder),
						OFC_CODE_LIST.SORT_ORDER.lessThan(oldSortOrder)
					).execute();
			}
			//set item sort order to final value
			dsl.update(OFC_CODE_LIST)
				.set(OFC_CODE_LIST.SORT_ORDER, newSortOrder)
				.where(OFC_CODE_LIST.ID.equal(item.getSystemId()))
				.execute();
			
			if ( isCacheInUse(list) ) {
				cache.clearItemsByCodeList(list);
			}
		}
	}

	public void delete(PersistedCodeListItem item) {
		CodeList codeList = item.getCodeList();

		JooqDSLContext dsl = dsl(null);
		dsl.deleteQuery(item.getSystemId()).execute();

		if ( dsl.getDialect() == SQLDialect.SQLITE ) {
			//SQLite foreign keys support disabled in order to have better performances
			//delete referencing items programmatically
			deleteInvalidParentReferenceItems(codeList);
		}
		if ( isCacheInUse(codeList) ) {
			cache.clearItemsByCodeList(codeList);
		}
	}

	public void deleteByCodeList(CodeList list) {
		DeleteConditionStep<OfcCodeListRecord> q = createDeleteQuery(list);
		q.execute();
		if ( isCacheInUse(list) ) {
			cache.clearItemsByCodeList(list);
		}
	}
	
	protected DeleteConditionStep<OfcCodeListRecord> createDeleteQuery(CodeList list) {
		JooqDSLContext jf = dsl(null);
		CollectSurvey survey = (CollectSurvey) list.getSurvey();
		DeleteConditionStep<OfcCodeListRecord> q = jf.delete(OFC_CODE_LIST)
			.where(
					OFC_CODE_LIST.SURVEY_ID.equal(survey.getId()),
					OFC_CODE_LIST.CODE_LIST_ID.equal(list.getId())
			);
		return q;
	}
	
	public void deleteBySurvey(int surveyId) {
		deleteBySurvey(surveyId, false);
	}
	
	public void deleteBySurveyWork(int surveyId) {
		deleteBySurvey(surveyId, true);
	}
	
	public void deleteBySurvey(int surveyId, boolean work) {
		JooqDSLContext jf = dsl(null);
		jf.delete(OFC_CODE_LIST)
			.where(OFC_CODE_LIST.SURVEY_ID.equal(surveyId))
			.execute();
		
		if ( isCacheInUse(work) ) {
			cache.clearItemsBySurvey(surveyId);
		}
	}
	
	public void deleteInvalidCodeListReferenceItems(CollectSurvey survey) {
		//create delete where condition
		Condition whereCondition = OFC_CODE_LIST.SURVEY_ID.equal(survey.getId());
		
		List<Integer> codeListsIds = new ArrayList<Integer>();
		if ( ! survey.getCodeLists().isEmpty() ) {
			//include items that belongs to detached code lists
	 		for (CodeList codeList : survey.getCodeLists()) {
				codeListsIds.add(codeList.getId());
			}
			whereCondition = whereCondition.and(OFC_CODE_LIST.CODE_LIST_ID.notIn(codeListsIds));
		}
		//execute delete
		JooqDSLContext jf = dsl(null);
		jf.delete(OFC_CODE_LIST).where(whereCondition).execute();
		
		if ( isCacheInUse(survey) ) {
			cache.clearItemsBySurvey(survey.getId());
		}
	}
	
	public void deleteInvalidParentReferenceItems(CodeList codeList) {
		CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
		int codeListId = codeList.getId();
		JooqDSLContext jf = dsl(null);
		jf.delete(OFC_CODE_LIST)
			.where(OFC_CODE_LIST.CODE_LIST_ID.eq(codeListId)
				.and(OFC_CODE_LIST.SURVEY_ID.eq(survey.getId()))
				.and(OFC_CODE_LIST.PARENT_ID.isNotNull())
				.and(OFC_CODE_LIST.PARENT_ID.notIn(
					jf.select(OFC_CODE_LIST.ID)
						.from(OFC_CODE_LIST)
						.where(OFC_CODE_LIST.CODE_LIST_ID.eq(codeListId))
						)
					)
				)
			.execute();
		
		if ( isCacheInUse(codeList) ) {
			cache.clearItemsByCodeList(codeList);
		}
	}
	
	public void removeVersioningInfo(CodeList codeList, ModelVersion version) {
		JooqDSLContext jf = dsl(null);
		CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
		int codeListId = codeList.getId();
		jf.update(OFC_CODE_LIST)
			.set(OFC_CODE_LIST.SINCE_VERSION_ID, (Integer) null)
			.where(OFC_CODE_LIST.SURVEY_ID.eq(survey.getId())
				.and(OFC_CODE_LIST.CODE_LIST_ID.eq(codeListId))
				.and(OFC_CODE_LIST.SINCE_VERSION_ID.eq(version.getId()))
				)
			.execute();

		jf.update(OFC_CODE_LIST)
			.set(OFC_CODE_LIST.DEPRECATED_VERSION_ID, (Integer) null)
			.where(OFC_CODE_LIST.SURVEY_ID.eq(survey.getId())
				.and(OFC_CODE_LIST.CODE_LIST_ID.eq(codeListId))
				.and(OFC_CODE_LIST.DEPRECATED_VERSION_ID.eq(version.getId()))
				)
			.execute();

		if ( isCacheInUse(codeList) ) {
			cache.clearItemsByCodeList(codeList);
		}
	}

	public void removeLabels(CollectSurvey survey, int fromLanguagePosition) {
		JooqDSLContext jf = dsl(null);
		List<CodeList> codeLists = survey.getCodeLists();
		for (CodeList codeList : codeLists) {
			int codeListId = codeList.getId();
			
			Map<TableField<OfcCodeListRecord, String>, String> updateFields = new HashMap<TableField<OfcCodeListRecord,String>, String>();
			for (int i = fromLanguagePosition - 1; i < LABEL_FIELDS.length; i++) {
				@SuppressWarnings("unchecked")
				TableField<OfcCodeListRecord, String> labelField = LABEL_FIELDS[i];
				updateFields.put(labelField, (String) null);
			}
			jf.update(OFC_CODE_LIST)
				.set(updateFields)
				.where(OFC_CODE_LIST.SURVEY_ID.eq(survey.getId())
					.and(OFC_CODE_LIST.CODE_LIST_ID.eq(codeListId))
					)
				.execute();
			
			if ( isCacheInUse(codeList) ) {
				cache.clearItemsByCodeList(codeList);
			}
		}
	}

	public void moveItems(int fromSurveyId, int toSurveyId) {
		JooqDSLContext jf = dsl(null);
		jf.update(OFC_CODE_LIST)
			.set(OFC_CODE_LIST.SURVEY_ID, toSurveyId)
			.where(OFC_CODE_LIST.SURVEY_ID.equal(fromSurveyId))
			.execute();
	}

	public List<PersistedCodeListItem> loadRootItems(CodeList codeList) {
		return loadChildItems(codeList, (Long) null, (ModelVersion) null);
	}
	
	public PersistedCodeListItem loadRootItem(CodeList codeList, String code) {
		return loadRootItem(codeList, code, (ModelVersion) null);
	}
	
	public PersistedCodeListItem loadRootItem(CodeList codeList, String code, ModelVersion version) {
		return loadItem(codeList, (Long) null, code, version);
	}
	
	public List<PersistedCodeListItem> loadChildItems(PersistedCodeListItem item) {
		return loadChildItems(item, (ModelVersion) null);
	}
	
	public List<PersistedCodeListItem> loadChildItems(PersistedCodeListItem item, ModelVersion version) {
		return loadChildItems(item.getCodeList(), item.getSystemId(), version);
	}
	
	protected List<PersistedCodeListItem> loadChildItems(CodeList codeList, Long parentItemId) {
		return loadChildItems(codeList, parentItemId, (ModelVersion) null);
	}
	
	protected List<PersistedCodeListItem> loadChildItems(CodeList codeList, Long parentItemId, ModelVersion version) {
		List<PersistedCodeListItem> items = null;
		boolean usingCache = isCacheInUse(codeList);
		if (usingCache) {
			items = cache.getItems(codeList, parentItemId);
		}
		if (items == null) {
			JooqDSLContext dsl = dsl(codeList);
			SelectQuery<Record> q = createSelectChildItemsQuery(dsl, codeList, parentItemId, true);
			Result<Record> result = q.fetch();
			items = dsl.fromResult(result);
			if (usingCache) {
				cache.putItems(codeList, parentItemId, items);
			}
		}
		return version == null ? items : version.filterApplicableItems(items);
	}
	
	public List<PersistedCodeListItem> loadItemsByLevel(CodeList list, int levelPosition) {
		JooqDSLContext jf = dsl(list);
		SelectQuery<Record> q = createSelectFromCodeListQuery(jf, list);
		q.addConditions(OFC_CODE_LIST.LEVEL.equal(levelPosition));
		Result<Record> result = q.fetch();
		return jf.fromResult(result);
	}
	
	/**
	 * Loads all the items and sorts them by level and sort_order
	 */
	private List<PersistedCodeListItem> loadAllItems(CodeList list) {
		JooqDSLContext jf = dsl(list);
		SelectQuery<Record> q = createSelectFromCodeListQuery(jf, list);
		q.addOrderBy(OFC_CODE_LIST.LEVEL, OFC_CODE_LIST.SORT_ORDER);
		Result<Record> result = q.fetch();
		return jf.fromResult(result);
	}
	
	public boolean isEmpty(CodeList list) {
		return ! hasChildItems(list, (Long) null);
	}

	public boolean hasChildItems(CodeList codeList, Long parentItemId) {
		JooqDSLContext jf = dsl(codeList);
		SelectQuery<Record> q = createSelectChildItemsQuery(jf, codeList, parentItemId, false);
		q.addSelect(DSL.count());
		Record record = q.fetchOne();
		Integer count = (Integer) record.getValue(0);
		return count > 0;
	}
	
	public boolean hasItemsInLevel(CodeList codeList, int level) {
		JooqDSLContext jf = dsl(codeList);
		SelectQuery<Record> q = createSelectFromCodeListQuery(jf, codeList);
		q.addSelect(DSL.count());
		q.addConditions(OFC_CODE_LIST.LEVEL.equal(level));
		Record record = q.fetchOne();
		Integer count = (Integer) record.getValue(0);
		return count > 0;
	}
	
	public void removeItemsInLevel(CodeList list, int level) {
		DeleteConditionStep<OfcCodeListRecord> q = createDeleteQuery(list);
		q.and(OFC_CODE_LIST.LEVEL.equal(level));
		q.execute();
	}

	public boolean hasQualifiableItems(CodeList codeList) {
		JooqDSLContext jf = dsl(codeList);
		CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
		SelectConditionStep<Record1<Integer>> q = jf.selectCount()
				.from(OFC_CODE_LIST)
				.where(
					OFC_CODE_LIST.SURVEY_ID.equal(survey.getId()),
					OFC_CODE_LIST.CODE_LIST_ID.equal(codeList.getId()),
					OFC_CODE_LIST.QUALIFIABLE.equal(Boolean.TRUE)
				);
		Record r = q.fetchOne();
		if ( r == null ) {
			return false;
		} else {
			Integer count = (Integer) r.getValue(0);
			return count > 0;
		}
	}
	
	public PersistedCodeListItem loadItem(CodeList codeList, Long parentItemId, String code, ModelVersion version) {
		boolean usingCache = isCacheInUse(codeList);
		if ( usingCache ) {
			PersistedCodeListItem item = cache.getItem(codeList, parentItemId, code, version);
			if (item == null) {
				//update cache
				loadChildItems(codeList, parentItemId, version);
				item = cache.getItem(codeList, parentItemId, code, version);
			}
			if (item != null) {
				return item;
			}
		}
		JooqDSLContext jf = dsl(codeList);
		SelectQuery<Record> q = createSelectChildItemsQuery(jf, codeList, parentItemId, false);
		q.addConditions(
				OFC_CODE_LIST.CODE.equal(code)
		);
		Result<Record> result = q.fetch();
		List<PersistedCodeListItem> list = jf.fromResult(result);
		
		List<PersistedCodeListItem> filteredByVersion = filterApplicableItems(list, version);
		
		PersistedCodeListItem item = filteredByVersion.isEmpty() ? null: filteredByVersion.get(0);

		return item;
	}

	public PersistedCodeListItem loadItem(CodeList codeList, int itemId) {
		JooqDSLContext dsl = dsl(codeList);
		SelectQuery<Record> q = createSelectFromCodeListQuery(dsl, codeList);
		q.addConditions(OFC_CODE_LIST.ITEM_ID.equal(itemId));
		Record record = q.fetchOne();
		return record == null ? null : dsl.fromRecord(record);
	}
	
	public PersistedCodeListItem loadItem(CodeList codeList, String code, ModelVersion version) {
		JooqDSLContext jf = dsl(codeList);
		SelectQuery<Record> q = createSelectFromCodeListQuery(jf, codeList);
		q.addConditions(OFC_CODE_LIST.CODE.equal(code));
		Result<Record> result = q.fetch();
		List<PersistedCodeListItem> list = jf.fromResult(result);
		List<PersistedCodeListItem> filteredByVersion = filterApplicableItems(list, version);
		if ( filteredByVersion.isEmpty() ) {
			return null;
		} else {
			return filteredByVersion.get(0);
		}
	}
	
	public void visitItems(CodeList list, Visitor<CodeListItem> visitor, ModelVersion version) {
		visitChildItems(list, null, visitor, version);
	}
	
	public void visitChildItems(CodeList list, Long parentItemId, Visitor<CodeListItem> visitor, ModelVersion version) {
		JooqDSLContext dsl = dsl(list);
		SelectQuery<Record> q = createSelectChildItemsQuery(dsl, list, parentItemId, true);
		Cursor<Record> cursor = null;
		try {
			cursor = q.fetchLazy();
			while (cursor.hasNext()) {
				Record r = cursor.fetchOne();
				PersistedCodeListItem item = dsl.fromRecord(r);
				if (version == null || version.isApplicable(item)) {
					visitor.visit(item);
					visitChildItems(list, item.getSystemId(), visitor, version);
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
		    }
		}
	}

	public void clearCache() {
		cache.clear();
	}
	
	protected List<PersistedCodeListItem> filterApplicableItems(List<PersistedCodeListItem> list, ModelVersion version) {
		if ( version == null ) {
			return list;
		} else {
			List<PersistedCodeListItem> appliable = version.filterApplicableItems(list);
			return appliable;
		}
	}
	
	protected int loadMinId(JooqDSLContext jf, int surveyId) {
		Integer result = jf.select(DSL.min(OFC_CODE_LIST.ID))
				.from(OFC_CODE_LIST)
				.where(OFC_CODE_LIST.SURVEY_ID.equal(surveyId))
				.fetchOne(0, Integer.class);
		return result == null ? 0: result.intValue();
	}

	protected long loadMaxId(JooqDSLContext jf) {
		Long result = jf.select(DSL.max(OFC_CODE_LIST.ID))
				.from(OFC_CODE_LIST)
				.fetchOne(0, Long.class);
		return result == null ? 0: result.longValue();
	}

	protected static SelectQuery<Record> createSelectChildItemsQuery(JooqDSLContext jf, CodeList codeList, Long parentItemId, boolean addOrderByClause) {
		SelectQuery<Record> q = createSelectFromCodeListQuery(jf, codeList);
		addFilterByParentItemConditions(q, codeList, parentItemId);
		if ( addOrderByClause ) {
			q.addOrderBy(OFC_CODE_LIST.SORT_ORDER);
		}
		return q;
	}

	protected static void addFilterByParentItemConditions(SelectQuery<Record> select,
			CodeList codeList, Long parentItemId) {
		Condition condition;
		if ( parentItemId == null ) {
			condition = OFC_CODE_LIST.PARENT_ID.isNull();
		} else {
			condition = OFC_CODE_LIST.PARENT_ID.equal(parentItemId);
		}
		select.addConditions(condition);
	}
	
	protected static SelectQuery<Record> createSelectFromCodeListQuery(JooqDSLContext dsl, CodeList codeList) {
		SelectQuery<Record> select = dsl.selectQuery();	
		select.addFrom(OFC_CODE_LIST);
		CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
		select.addConditions(
				OFC_CODE_LIST.SURVEY_ID.equal(survey.getId()),
				OFC_CODE_LIST.CODE_LIST_ID.equal(codeList.getId())
				);
		return select;
	}
	
	private boolean isCacheInUse(CodeList codeList) {
		CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
		return isCacheInUse(survey);
	}

	private boolean isCacheInUse(CollectSurvey survey) {
		return isCacheInUse(survey.isTemporary());
	}

	private boolean isCacheInUse(boolean temporarySurvey) {
		return useCache && ! temporarySurvey;
	}
	
	@Override
	protected JooqDSLContext dsl() {
		throw new UnsupportedOperationException();
	}
	
	protected JooqDSLContext dsl(CodeList codeList) {
		return new JooqDSLContext(getConfiguration(), codeList);
	}
	
	public long nextSystemId() {
		JooqDSLContext jf = dsl(null);
		return jf.nextId();
	}
	
	public boolean isUseCache() {
		return useCache;
	}
	
	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}
	
	private int findMin(List<Integer> values) {
		if (values.isEmpty()) {
			return 0;
		}
		Integer[] valuesArr = values.toArray(new Integer[values.size()]);
		Arrays.sort(valuesArr);
		return valuesArr[0];
	}

	protected static class JooqDSLContext extends MappingDSLContext<Long, PersistedCodeListItem> {

		private static final long serialVersionUID = 1L;
		
		private CodeList codeList;
		
		public JooqDSLContext(Configuration config, CodeList codeList) {
			super(config, OFC_CODE_LIST.ID, OFC_CODE_LIST_ID_SEQ, PersistedCodeListItem.class);
			this.codeList = codeList;
		}
		
		@Override
		protected PersistedCodeListItem newEntity() {
			throw new UnsupportedOperationException();
		}
		
		protected PersistedCodeListItem newEntity(int itemId, int level) {
			return new PersistedCodeListItem(codeList, itemId, level);
		}
		
		@Override
		public PersistedCodeListItem fromRecord(Record record) {
			int itemId = record.getValue(OFC_CODE_LIST.ITEM_ID);
			int level = record.getValue(OFC_CODE_LIST.LEVEL);
			PersistedCodeListItem entity = newEntity(itemId, level);
			fromRecord(record, entity);
			return entity;
		}

		@Override
		public void fromRecord(Record r, PersistedCodeListItem i) {
			i.setSystemId(r.getValue(OFC_CODE_LIST.ID));
			i.setParentId(r.getValue(OFC_CODE_LIST.PARENT_ID));
			i.setSortOrder(r.getValue(OFC_CODE_LIST.SORT_ORDER));
			i.setCode(r.getValue(OFC_CODE_LIST.CODE));
			i.setQualifiable(r.getValue(OFC_CODE_LIST.QUALIFIABLE));
			i.setSinceVersion(extractModelVersion(r, i, OFC_CODE_LIST.SINCE_VERSION_ID));
			i.setDeprecatedVersion(extractModelVersion(r, i, OFC_CODE_LIST.DEPRECATED_VERSION_ID));
			i.setImageFileName(r.getValue(OFC_CODE_LIST.IMAGE_FILE_NAME));
			i.setColor(r.getValue(OFC_CODE_LIST.COLOR));
			extractLabels(r, i);
			extractDescriptions(r, i);
		}

		protected ModelVersion extractModelVersion(Record r,
				SurveyObject surveyObject,
				TableField<OfcCodeListRecord, Integer> versionIdField) {
			Survey survey = surveyObject.getSurvey();
			Integer versionId = r.getValue(versionIdField);
			ModelVersion version = versionId == null ? null: survey.getVersionById(versionId);
			return version;
		}
		
		protected void extractLabels(Record r, PersistedCodeListItem item) {
			Survey survey = codeList.getSurvey();
			item.removeAllLabels();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < languages.size(); i++) {
				String lang = languages.get(i);
				@SuppressWarnings("unchecked")
				String label = r.<String>getValue(LABEL_FIELDS[i]);
				item.setLabel(lang, label);
			}
		}
		
		protected void extractDescriptions(Record r, PersistedCodeListItem item) {
			Survey survey = codeList.getSurvey();
			item.removeAllDescriptions();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < languages.size(); i++) {
				String lang = languages.get(i);
				@SuppressWarnings("unchecked")
				String description = r.<String>getValue(DESCRIPTION_FIELDS[i]);
				item.setDescription(lang, description);
			}
		}

		@Override
		public void fromObject(PersistedCodeListItem item, StoreQuery<?> q) {
			q.addValue(OFC_CODE_LIST.ID, item.getSystemId());
			CollectSurvey survey = item.getSurvey();
			Integer surveyId = survey.getId();
			q.addValue(OFC_CODE_LIST.SURVEY_ID, surveyId);
			q.addValue(OFC_CODE_LIST.CODE_LIST_ID, item.getCodeList().getId());
			q.addValue(OFC_CODE_LIST.ITEM_ID, item.getId());
			q.addValue(OFC_CODE_LIST.PARENT_ID, item.getParentId());
			Integer sortOrder = item.getSortOrder();
			if ( sortOrder == null ) {
				sortOrder = nextSortOrder(item);
				item.setSortOrder(sortOrder);
			}
			q.addValue(OFC_CODE_LIST.LEVEL, item.getLevel());
			q.addValue(OFC_CODE_LIST.SORT_ORDER, sortOrder);
			q.addValue(OFC_CODE_LIST.CODE, item.getCode());
			q.addValue(OFC_CODE_LIST.QUALIFIABLE, item.isQualifiable());
			Integer sinceVersionId = item.getSinceVersion() == null ? null: item.getSinceVersion().getId();
			q.addValue(OFC_CODE_LIST.SINCE_VERSION_ID, sinceVersionId);
			Integer deprecatedVersionId = item.getDeprecatedVersion() == null ? null: item.getDeprecatedVersion().getId();
			q.addValue(OFC_CODE_LIST.DEPRECATED_VERSION_ID, deprecatedVersionId);
			q.addValue(OFC_CODE_LIST.IMAGE_FILE_NAME, item.getImageFileName());
			q.addValue(OFC_CODE_LIST.COLOR, item.getColor());
			addLabelValues(q, item);
			addDescriptionValues(q, item);
		}
		
		public Insert<OfcCodeListRecord> createInsertStatement() {
			Object[] valuesPlaceholders = new String[POJO_FIELDS.length];
			Arrays.fill(valuesPlaceholders, "?");
			return insertInto(OFC_CODE_LIST, POJO_FIELDS).values(valuesPlaceholders);
		}
		
		protected List<Object> extractValues(PersistedCodeListItem item) {
			CodeList list = item.getCodeList();
			CollectSurvey survey = item.getSurvey();
			Integer surveyId = survey.getId();
			ModelVersion sinceVersion = item.getSinceVersion();
			Integer sinceVersionId = sinceVersion == null ? null: sinceVersion.getId();
			ModelVersion deprecatedVersion = item.getDeprecatedVersion();
			Integer deprecatedVersionId = deprecatedVersion == null ? null: deprecatedVersion.getId();
			List<Object> values = new ArrayList<Object>(POJO_FIELDS.length);
			values.addAll(Arrays.<Object>asList(item.getSystemId(), surveyId, 
					list.getId(), item.getId(), item.getParentId(), item.getLevel(), item.getSortOrder(), item.getCode(), 
					item.isQualifiable(), sinceVersionId, deprecatedVersionId, item.getImageFileName(), item.getColor()));
			values.addAll(getLabelValues(item));
			values.addAll(getDescriptionValues(item));
			return values;
		}

		private Integer nextSortOrder(PersistedCodeListItem item) {
			SelectQuery<Record> select = createSelectFromCodeListQuery(this, codeList);
			select.addSelect(DSL.max(OFC_CODE_LIST.SORT_ORDER));
			addFilterByParentItemConditions(select, codeList, item.getParentId());
			Record record = select.fetchOne();
			Integer max = (Integer) record.getValue(0);
			return max == null ? 1: max + 1;
		}

		protected void addLabelValues(StoreQuery<?> q, PersistedCodeListItem item) {
			List<String> values = getLabelValues(item);
			for (int i = 0; i < LABEL_FIELDS.length; i++) {
				@SuppressWarnings("unchecked")
				TableField<?, String> field = LABEL_FIELDS[i];
				String label = values.get(i);
				q.addValue(field, label);
			}
		}
		
		protected List<String> getLabelValues(PersistedCodeListItem item) {
			int size = LABEL_FIELDS.length;
			List<String> result = new ArrayList<String>(size);
			Survey survey = item.getSurvey();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < size; i++) {
				String label;
				if ( i < languages.size() ) {
					String lang = languages.get(i);
					label = item.getLabel(lang);
				} else {
					label = null;
				}
				result.add(label);
			}
			return result;
		}

		protected void addDescriptionValues(StoreQuery<?> q, PersistedCodeListItem item) {
			List<String> values = getDescriptionValues(item);
			for (int i = 0; i < DESCRIPTION_FIELDS.length; i++) {
				@SuppressWarnings("unchecked")
				TableField<?, String> field = DESCRIPTION_FIELDS[i];
				String description = values.get(i);
				q.addValue(field, description);
			}
		}

		protected List<String> getDescriptionValues(PersistedCodeListItem item) {
			int size = DESCRIPTION_FIELDS.length;
			List<String> result = new ArrayList<String>(size);
			Survey survey = item.getSurvey();
			List<String> languages = survey.getLanguages();
			for (int i = 0; i < size; i++) {
				String description;
				if ( i < languages.size() ) {
					String lang = languages.get(i);
					description = item.getDescription(lang);
				} else {
					description = null;
				}
				result.add(description);
			}
			return result;
		}
		
		@Override
		protected void setId(PersistedCodeListItem t, Long id) {
			t.setSystemId(id);
		}

		@Override
		protected Long getId(PersistedCodeListItem t) {
			return t.getSystemId();
		}
	}

}
