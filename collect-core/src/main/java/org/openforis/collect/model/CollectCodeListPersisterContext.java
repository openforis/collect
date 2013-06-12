package org.openforis.collect.model;

import org.openforis.idm.metamodel.CodeListItemPersister;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.SurveyCodeListPersisterContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectCodeListPersisterContext implements
		SurveyCodeListPersisterContext {
	
	private CodeListItemPersister externalCodeListPersister;
	private ExternalCodeListProvider externalCodeListProvider;
	
	@Override
	public ExternalCodeListProvider getExternalCodeListProvider() {
		return externalCodeListProvider;
	}

	public void setExternalCodeListProvider(
			ExternalCodeListProvider externalCodeListProvider) {
		this.externalCodeListProvider = externalCodeListProvider;
	}
	
	@Override
	public CodeListItemPersister getExternalCodeListPersister() {
		return externalCodeListPersister;
	}

	public void setExternalCodeListPersister(
			CodeListItemPersister externalCodeListPersister) {
		this.externalCodeListPersister = externalCodeListPersister;
	}
}
