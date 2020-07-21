/**
 * 
 */
package org.openforis.collect.manager;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

import java.util.Collections;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.collect.persistence.SamplingDesignDao;
import org.openforis.commons.collection.Visitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 * 
 */
@Transactional(readOnly=true, propagation=SUPPORTS)
public class SamplingDesignManager {
	
	//private final Logger log = Logger.getLogger(SamplingDesignManager.class);
	
	@Autowired
	private SamplingDesignDao samplingDesignDao;

	public SamplingDesignItem loadById(long id) {
		return samplingDesignDao.loadById(id);
	}

	public SamplingDesignSummaries loadBySurvey(int surveyId) {
		return loadBySurvey(surveyId, 0, Integer.MAX_VALUE);
	}

	public SamplingDesignSummaries loadBySurvey(int surveyId, Integer upToLevel) {
		return loadBySurvey(surveyId, upToLevel, 0, Integer.MAX_VALUE);
	}

	public SamplingDesignSummaries loadBySurvey(int surveyId, int offset, int maxRecords) {
		return loadBySurvey(surveyId, null, offset, maxRecords);
	}

	public SamplingDesignSummaries loadBySurvey(int surveyId, Integer upToLevel, int offset, int maxRecords) {
		int totalCount = samplingDesignDao.countBySurvey(surveyId);
		List<SamplingDesignItem> items;
		if ( totalCount > 0 ) {
			items = samplingDesignDao.loadItems(surveyId, upToLevel, offset, maxRecords); 
		} else {
			items = Collections.emptyList();
		}
		return new SamplingDesignSummaries(totalCount, items);
	}
	
	public SamplingDesignItem loadItem(int surveyId, List<String> parentKeys) {
		return loadItem(surveyId, parentKeys.toArray(new String[parentKeys.size()]));
	}
	
	public SamplingDesignItem loadItem(int surveyId, String... parentKeys) {
		return samplingDesignDao.loadItem(surveyId, parentKeys);
	}
	
	public List<SamplingDesignItem> loadChildItems(int surveyId, String... parentKeys) {
		return samplingDesignDao.loadChildItems(surveyId, parentKeys);
	}

	public List<SamplingDesignItem> loadChildItems(int surveyId, List<String> parentKeys) {
		return loadChildItems(surveyId, parentKeys.toArray(new String[parentKeys.size()]));
	}
	
	public boolean hasSamplingDesign(CollectSurvey survey) {
		Integer id = survey.getId();
		if ( id == null ) {
			return false;
		} else {
			return countBySurvey(id) > 0;
		}
	}
	
	public int countBySurvey(int surveyId) {
		return samplingDesignDao.countBySurvey(surveyId);
	}
	
	public int countMaxChildrenInLevel(int surveyId, int level) {
		return samplingDesignDao.countMaxByLevel(surveyId, level);
	}

	public void visitItems(int surveyId, Integer upToLevel, Visitor<SamplingDesignItem> visitor) {
		samplingDesignDao.visitItems(surveyId, upToLevel, visitor);
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void save(SamplingDesignItem item) {
		if ( item.getId() == null ) {
			samplingDesignDao.insert(item);
		} else {
			samplingDesignDao.update(item);
		}
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void delete(SamplingDesignItem item) {
		samplingDesignDao.delete(item.getId());
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void deleteBySurvey(int surveyId) {
		samplingDesignDao.deleteBySurvey(surveyId);
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void moveSamplingDesign(int fromSurveyId, int toSurveyId) {
		samplingDesignDao.deleteBySurvey(toSurveyId);
		samplingDesignDao.moveItems(fromSurveyId, toSurveyId);
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void copySamplingDesign(int fromSurveyId, int toSurveyId) {
		samplingDesignDao.copyItems(fromSurveyId, toSurveyId);
	}

	@Transactional(readOnly=false, propagation=REQUIRED)
	public void insert(CollectSurvey survey, List<SamplingDesignItem> items, boolean overwriteAll) {
		if ( overwriteAll ) {
			deleteBySurvey(survey.getId());
		}
		samplingDesignDao.insert(items);
	}
}