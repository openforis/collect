/**
 * 
 */
package org.openforis.collect.manager;

import org.openforis.collect.persistence.SurveyDAO;
import org.openforis.idm.metamodel.Survey;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class SurveyManager {
//	private EntityManager entityManager;
	
	@Autowired
	private SurveyDAO surveyDAO;
	
	public Survey load(String name) {
		Survey survey = surveyDAO.load(name);
		return survey;
	}

	
}
