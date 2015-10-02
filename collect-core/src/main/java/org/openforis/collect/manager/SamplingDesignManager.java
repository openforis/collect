/**
 * 
 */
package org.openforis.collect.manager;

import java.util.Collections;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.collect.persistence.SamplingDesignDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 * 
 */
public class SamplingDesignManager {
	
	//private final Log log = LogFactory.getLog(SamplingDesignManager.class);
	
	@Autowired
	private SamplingDesignDao samplingDesignDao;

	@Transactional
	public SamplingDesignItem loadById(int id) {
		return samplingDesignDao.loadById(id);
	}

	@Transactional
	public SamplingDesignSummaries loadBySurvey(int surveyId) {
		return loadBySurvey(surveyId, 0, Integer.MAX_VALUE);
	}

	@Transactional
	public SamplingDesignSummaries loadBySurvey(int surveyId, int offset, int maxRecords) {
		int totalCount = samplingDesignDao.countBySurvey(surveyId);
		List<SamplingDesignItem> records;
		if ( totalCount > 0 ) {
			records = samplingDesignDao.loadItems(surveyId, offset, maxRecords); 
		} else {
			records = Collections.emptyList();
		}
		SamplingDesignSummaries result = new SamplingDesignSummaries(totalCount, records);
		return result;
	}
	
	@Transactional
	public boolean hasSamplingDesign(CollectSurvey survey) {
		Integer id = survey.getId();
		if ( id == null ) {
			return false;
		} else {
			return countBySurvey(id) > 0;
		}
	}
	
	@Transactional
	public int countBySurvey(int surveyId) {
		return samplingDesignDao.countBySurvey(surveyId);
	}

	@Transactional
	public void save(SamplingDesignItem item) {
		if ( item.getId() == null ) {
			samplingDesignDao.insert(item);
		} else {
			samplingDesignDao.update(item);
		}
	}
	
	@Transactional
	public void delete(SamplingDesignItem item) {
		Integer id = item.getId();
		samplingDesignDao.delete(id);
	}
	
	@Transactional
	public void deleteBySurvey(int surveyId) {
		samplingDesignDao.deleteBySurvey(surveyId);
	}
	
	@Transactional
	public void moveSamplingDesign(int fromSurveyId, int toSurveyId) {
		samplingDesignDao.deleteBySurvey(toSurveyId);
		samplingDesignDao.moveItems(fromSurveyId, toSurveyId);
	}
	
	@Transactional
	public void copySamplingDesign(int fromSurveyId, int toSurveyId) {
		samplingDesignDao.copyItems(fromSurveyId, toSurveyId);
	}

	@Transactional
	public void insert(CollectSurvey survey, List<SamplingDesignItem> items, boolean overwriteAll) {
		if ( overwriteAll ) {
			deleteBySurvey(survey.getId());
		}
		samplingDesignDao.insert(items);
	}
}
