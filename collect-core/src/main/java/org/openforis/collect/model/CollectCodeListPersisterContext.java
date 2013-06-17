package org.openforis.collect.model;

import org.openforis.idm.metamodel.CodeListItemPersister;
import org.openforis.idm.metamodel.SurveyCodeListPersisterContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectCodeListPersisterContext implements
		SurveyCodeListPersisterContext {
	
	private CodeListItemPersister codeListPersister;
	
	@Override
	public CodeListItemPersister getCodeListPersister() {
		return codeListPersister;
	}
	
	public void setCodeListPersister(CodeListItemPersister codeListPersister) {
		this.codeListPersister = codeListPersister;
	}
}
