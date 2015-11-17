package org.openforis.collect.remoting.service;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListSessionService {
	
	@Autowired
	private CodeListManager codeListManager;
	@Autowired
	private RecordSessionManager sessionManager;
	
	@Secured("ROLE_ADMIN")
	public boolean isEditedSurveyCodeListEmpty(int codeListId) {
		CollectSurvey editedSurvey = sessionManager.getActiveDesignerSurvey();
		CodeList list = editedSurvey.getCodeListById(codeListId);
		boolean result = codeListManager.isEmpty(list);
		return result;
	}

}
