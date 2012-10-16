/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.form.SurveyObjectFormObject;
import org.openforis.collect.designer.form.ModelVersionFormObject;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.ModelVersion;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class VersioningVM extends SurveyObjectBaseVM<ModelVersion> {
	
	private static final String VERSIONS_UPDATED_GLOBAL_COMMAND = "versionsUpdated";

	@Override
	protected List<ModelVersion> getItemsInternal() {
		CollectSurvey survey = getSurvey();
		List<ModelVersion> versions = survey.getVersions();
		return versions;
	}
	
	protected ModelVersion createItemInstance() {
		ModelVersion instance = survey.createModelVersion();
		return instance;
	}
	
	@Override
	protected void addNewItemToSurvey() {
		CollectSurvey survey = getSurvey();
		survey.addVersion(editedItem);
	}
	
	@Override
	protected void deleteItemFromSurvey(ModelVersion item) {
		CollectSurvey survey = getSurvey();
		survey.removeVersion(item);
		dispatchVersionsUpdatedCommand();
	}

	@Override
	protected void moveSelectedItem(int indexTo) {
		CollectSurvey survey = getSurvey();
		survey.moveVersion(selectedItem, indexTo);
	}
	
	@Override
	protected SurveyObjectFormObject<ModelVersion> createFormObject() {
		return new ModelVersionFormObject();
	}
	
	@Override
	@Command
	@NotifyChange({"editedItem","selectedItem"})
	public void applyChanges() {
		super.applyChanges();
		dispatchVersionsUpdatedCommand();
	}

	protected void dispatchVersionsUpdatedCommand() {
		BindUtils.postGlobalCommand(null, null, VERSIONS_UPDATED_GLOBAL_COMMAND, null);
	}

}
