/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorQueryGroup;
import org.openforis.collect.datacleansing.persistence.DataErrorQueryGroupDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 *
 */
@Component
public class DataErrorQueryGroupManager extends AbstractSurveyObjectManager<DataErrorQueryGroup, DataErrorQueryGroupDao> {

	@Autowired
	private DataErrorQueryManager dataErrorQueryManager;
	
	@Override
	@Autowired
	@Qualifier("dataErrorQueryGroupDao")
	public void setDao(DataErrorQueryGroupDao dao) {
		super.setDao(dao);
	}
	
	public Set<DataErrorQueryGroup> loadByQuery(DataErrorQuery query) {
		Set<DataErrorQueryGroup> groups = dao.loadGroupsByQuery(query);
		initializeItems(groups);
		return groups;
	}
	
	@Override
	@Transactional
	public void save(DataErrorQueryGroup group) {
		List<Integer> queryIds = new ArrayList<Integer>();
		for (DataErrorQuery query : group.getQueries()) {
			queryIds.add(query.getId());
		}
		if (group.getId() != null) {
			dao.deleteQueryAssociations(group);
		}
		super.save(group);
		
		dao.insertQueryAssociations(group, queryIds);
		
		initializeItem(group);
	}
	
	@Override
	public void delete(DataErrorQueryGroup chain) {
		dao.deleteQueryAssociations(chain);
		super.delete(chain);
	}
	
	@Override
	protected void initializeItem(DataErrorQueryGroup group) {
		super.initializeItem(group);
		group.removeAllQueries();
		List<Integer> queryIds = dao.loadQueryIds(group);
		for (Integer queryId : queryIds) {
			DataErrorQuery query = dataErrorQueryManager.loadById((CollectSurvey) group.getSurvey(), queryId);
			group.addQuery(query);
		}
	}
	
}
