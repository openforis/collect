package org.openforis.collect.web.controller;

import java.util.List;

import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.web.PersistedObjectForm;
import org.openforis.idm.metamodel.PersistedSurveyObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AbstractSurveyObjectEditFormController<T extends PersistedSurveyObject, 
			F extends PersistedObjectForm<T>, 
			M extends AbstractSurveyObjectManager<T, ?>>  
			extends AbstractPersistedObjectEditFormController<T, F, M> {
	
	@Autowired
	@Qualifier("sessionManager")
	protected SessionManager sessionManager;
	
	protected abstract T createItemInstance(CollectSurvey survey);
	
	@Override
	protected T createItemInstance() {
		throw new UnsupportedOperationException();
	};
	
	@Override
	protected T loadOrCreateItem(F form) {
		T item;
		if (form.getId() == null) {
			CollectSurvey survey = getActiveSurvey();
			item = createItemInstance(survey);
		} else {
			item = loadItem(form.getId());
		}
		return item;
	}
	
	@Override
	protected T loadItem(int id) {
		CollectSurvey survey = getActiveSurvey();
		return itemManager.loadById(survey, id);
	}
	
	protected List<T> loadAllItems() {
		CollectSurvey survey = getActiveSurvey();
		List<T> items = itemManager.loadBySurvey(survey);
		return items;
	}
	
	protected CollectSurvey getActiveSurvey() {
		return sessionManager.getActiveSurvey();
	}

}
