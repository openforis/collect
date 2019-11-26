package org.openforis.collect.datacleansing;

import java.util.UUID;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.PersistedSurveyObject;

public abstract class DataCleansingItem extends PersistedSurveyObject<Integer> {

	private static final long serialVersionUID = 1L;

	protected DataCleansingItem(CollectSurvey survey) {
		super(survey);
	}
	
	protected DataCleansingItem(CollectSurvey survey, UUID uuid) {
		super(survey, uuid);
	}

}
