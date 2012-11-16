/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zkplus.databind.BindingListModelList;

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
	
	public List<ModelVersion> getFormVersions() {
		CollectSurvey survey = getSurvey();
		if ( survey == null ) {
			//TODO session expired...?
			return null;
		} else {
			List<ModelVersion> result = new ArrayList<ModelVersion>(survey.getVersions());
			return new BindingListModelList<ModelVersion>(result, false);
		}
	}
	
}
