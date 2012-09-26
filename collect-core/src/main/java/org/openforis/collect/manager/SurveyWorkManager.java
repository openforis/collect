package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.ui.UIConfiguration;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.SurveyWorkDao;
import org.openforis.idm.metamodel.Configuration;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyWorkManager {
	
	@Autowired
	private SurveyWorkDao surveyWorkDao;
	
	public CollectSurvey createSurvey() {
		CollectSurvey survey = new CollectSurvey();
		Schema schema = new Schema();
		survey.setSchema(schema);
		Configuration config = new UIConfiguration();
		survey.addConfiguration(config);
		return survey;
	}
	
	public List<CollectSurvey> getAll() {
		List<CollectSurvey> surveys = surveyWorkDao.loadAll();
		return CollectionUtil.unmodifiableList(surveys );
	}
	
	public CollectSurvey load(int id) {
		CollectSurvey survey = surveyWorkDao.load(id);
		return survey;
	}
	
	public void save(CollectSurvey survey) throws SurveyImportException {
		Integer id = survey.getId();
		if ( id == null ) {
			surveyWorkDao.insert(survey);
		} else {
			surveyWorkDao.update(survey);
		}
	}
}
