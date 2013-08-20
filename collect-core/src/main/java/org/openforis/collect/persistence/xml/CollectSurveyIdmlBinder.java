package org.openforis.collect.persistence.xml;

import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectSurveyIdmlBinder extends SurveyIdmlBinder {

	public CollectSurveyIdmlBinder(SurveyContext surveyContext) {
		super(surveyContext);
		addApplicationOptionsBinder(new UIOptionsBinder());
	}
	
}
