/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.List;
import java.util.Set;

import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryGroup;
import org.openforis.collect.datacleansing.persistence.DataQueryGroupDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.commons.collection.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 *
 */
@Component
public class DataQueryGroupManager extends AbstractSurveyObjectManager<DataQueryGroup, DataQueryGroupDao> {

	@Autowired
	private DataQueryManager dataQueryManager;
	
	public Set<DataQueryGroup> loadByQuery(DataQuery query) {
		Set<DataQueryGroup> groups = dao.loadGroupsByQuery(query);
		initializeItems(groups);
		return groups;
	}
	
	@Override
	@Transactional
	public DataQueryGroup save(DataQueryGroup group, User activeUser) {
		List<Integer> queryIds = CollectionUtils.project(group.getQueries(), "id");
		if (group.getId() != null) {
			dao.deleteQueryAssociations(group);
		}
		super.save(group, activeUser);
		
		dao.insertQueryAssociations(group, queryIds);
		
		initializeItem(group);
		
		return group;
	}
	
	@Override
	@Transactional
	public void delete(DataQueryGroup group) {
		dao.deleteQueryAssociations(group);
		super.delete(group);
	}
	
	@Override
	public void deleteBySurvey(CollectSurvey survey) {
		dao.deleteQueryAssociations(survey);
		super.deleteBySurvey(survey);
	}
	
	@Override
	protected void initializeItem(DataQueryGroup group) {
		super.initializeItem(group);
		group.removeAllQueries();
		List<Integer> queryIds = dao.loadQueryIds(group);
		for (Integer queryId : queryIds) {
			DataQuery query = dataQueryManager.loadById((CollectSurvey) group.getSurvey(), queryId);
			group.addQuery(query);
		}
	}
	
}
