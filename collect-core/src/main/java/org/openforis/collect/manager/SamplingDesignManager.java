/**
 * 
 */
package org.openforis.collect.manager;

import java.util.Collections;
import java.util.List;

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
		return load(false, surveyId, offset, maxRecords);
	}
	
	@Transactional
	public SamplingDesignSummaries loadBySurveyWork(int surveyWorkId) {
		return loadBySurveyWork(surveyWorkId, 0, Integer.MAX_VALUE);
	}
	
	@Transactional
	public SamplingDesignSummaries loadBySurveyWork(int surveyWorkId, int offset, int maxRecords) {
		return load(true, surveyWorkId, offset, maxRecords);
	}
	
	@Transactional
	protected SamplingDesignSummaries load(boolean work, int surveyId, int offset, int maxRecords) {
		int totalCount = work ? samplingDesignDao.countPerSurveyWork(surveyId): samplingDesignDao.countPerSurvey(surveyId);
		List<SamplingDesignItem> records;
		if ( totalCount > 0 ) {
			records = work ? samplingDesignDao.loadItemsBySurveyWork(surveyId, offset, maxRecords): 
				samplingDesignDao.loadItemsBySurvey(surveyId, offset, maxRecords);;
		} else {
			records = Collections.emptyList();
		}
		SamplingDesignSummaries result = new SamplingDesignSummaries(totalCount, records);
		return result;
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
	public void deleteBySurveyWork(int surveyId) {
		samplingDesignDao.deleteBySurveyWork(surveyId);
	}
	
	@Transactional
	public void publishSamplingDesign(Integer surveyWorkId, int publishedSurveyId) {
		List<SamplingDesignItem> items = samplingDesignDao.loadItemsBySurveyWork(surveyWorkId, 0, Integer.MAX_VALUE);
		samplingDesignDao.deleteBySurvey(publishedSurveyId);
		for (SamplingDesignItem item : items) {
			item.setSurveyWorkId(null);
			item.setSurveyId(publishedSurveyId);
			samplingDesignDao.update(item);
		}
	}
	
	@Transactional
	public void duplicateSamplingDesignForWork(int publishedSurveyId, Integer surveyWorkId) {
		List<SamplingDesignItem> items = samplingDesignDao.loadItemsBySurvey(publishedSurveyId, 0, Integer.MAX_VALUE);
		for (SamplingDesignItem item : items) {
			item.setId(null);
			item.setSurveyId(null);
			item.setSurveyWorkId(surveyWorkId);
			samplingDesignDao.insert(item);
		}
	}
}
