package org.openforis.collect.model;

import org.openforis.idm.metamodel.ExternalCodeListPersister;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.SurveyCodeListPersisterContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectCodeListPersisterContext implements
		SurveyCodeListPersisterContext {
	
	private ExternalCodeListPersister externalCodeListPersister;
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
	public ExternalCodeListPersister getExternalCodeListPersister() {
		return externalCodeListPersister;
	}

	public void setExternalCodeListPersister(
			ExternalCodeListPersister externalCodeListPersister) {
		this.externalCodeListPersister = externalCodeListPersister;
	}
}
