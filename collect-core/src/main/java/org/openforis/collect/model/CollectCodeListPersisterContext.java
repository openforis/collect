package org.openforis.collect.model;

import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.SurveyCodeListPersisterContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectCodeListPersisterContext implements
		SurveyCodeListPersisterContext {
	
	private CodeListService codeListService;
	
	@Override
	public CodeListService getCodeListService() {
		return codeListService;
	}
	
	public void setCodeListService(CodeListService codeListService) {
		this.codeListService = codeListService;
	}
}
