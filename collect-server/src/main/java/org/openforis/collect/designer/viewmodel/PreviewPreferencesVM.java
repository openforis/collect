/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;

/**
 * @author S. Ricci
 *
 */
public class PreviewPreferencesVM extends SurveyBaseVM {

	public List<EntityDefinition> getRootEntities() {
		CollectSurvey survey = getSurvey();
		if ( survey == null ) {
			//TODO session expired...?
			return null;
		} else {
			Schema schema = survey.getSchema();
			List<EntityDefinition> result = schema.getRootEntityDefinitions();
			return result;
		}
	}
	
}
