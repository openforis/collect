/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.openforis.collect.designer.form.ModelVersionFormObject;
import org.openforis.collect.designer.form.SurveyObjectFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.VersionableSurveyObject;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class VersioningVM extends SurveyObjectPopUpVM<ModelVersion> {
	
	@WireVariable
	private SurveyManager surveyManager;
	
	private Window confirmDeletePopUp;
	
	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
	}
	
	@Override
	protected List<ModelVersion> getItemsInternal() {
		CollectSurvey survey = getSurvey();
		List<ModelVersion> versions = survey.getSortedVersions();
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
		surveyManager.removeVersion(survey, item);
		dispatchVersionsUpdatedCommand();
	}

	@Command
	public void deleteVersioning(@BindingParam("item") final ModelVersion item) {
		List<VersionableSurveyObject> references = getReferences(item);
		if ( references.isEmpty() ) {
			super.deleteItem(item);
		} else {
			String title = Labels.getLabel("survey.versioning.delete.confirm_title");
			String message = Labels.getLabel("survey.versioning.delete.confirm_in_use");
			confirmDeletePopUp = SurveyErrorsPopUpVM.openPopUp(title, message, 
					references, new MessageUtil.CompleteConfirmHandler() {
				@Override
				public void onOk() {
					performDeleteItem(item);
					closeConfirmDeletePopUp();
				}
				@Override
				public void onCancel() {
					closeConfirmDeletePopUp();
				}
			});
		}
	}
	
	@Override
	protected String getConfirmDeleteMessageKey() {
		return "survey.versioning.delete.confirm_message";
	}
	
	protected void closeConfirmDeletePopUp() {
		closePopUp(confirmDeletePopUp);
		confirmDeletePopUp = null;
	}

	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {
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
	public void applyChanges(@ContextParam(ContextType.BINDER) Binder binder) {
		super.applyChanges(binder);
		dispatchVersionsUpdatedCommand();
	}
	
	protected List<VersionableSurveyObject> getReferences(ModelVersion version) {
		List<VersionableSurveyObject> referencesInSchema = getReferencesInSchema(version);
		List<VersionableSurveyObject> referencesInCodeLists = getReferencesInCodeLists(version);
		List<VersionableSurveyObject> result = new ArrayList<VersionableSurveyObject>();
		result.addAll(referencesInSchema);
		result.addAll(referencesInCodeLists);
		return result;
	}

	protected List<VersionableSurveyObject> getReferencesInSchema(ModelVersion version) {
		List<VersionableSurveyObject> references = new ArrayList<VersionableSurveyObject>();
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntities = schema.getRootEntityDefinitions();
		Stack<NodeDefinition> stack = new Stack<NodeDefinition>();
		stack.addAll(rootEntities);
		while ( ! stack.isEmpty() ) {
			NodeDefinition defn = stack.pop();
			if (isVersionInUse(version, defn) ) {
				references.add(defn);
			}
			if ( defn instanceof EntityDefinition ) {
				stack.addAll(((EntityDefinition) defn).getChildDefinitions());
			}
		}
		return references;
	}
	
	protected List<VersionableSurveyObject> getReferencesInCodeLists(ModelVersion version) {
		List<VersionableSurveyObject> references = new ArrayList<VersionableSurveyObject>();
		List<CodeList> codeLists = survey.getCodeLists();
		for (CodeList codeList : codeLists) {
			if ( isVersionInUse(version, codeList) ) {
				references.add(codeList);
			}
			if ( ! codeList.isExternal() ) {
				List<CodeListItem> items = codeList.getItems();
				Stack<CodeListItem> itemsStack = new Stack<CodeListItem>();
				itemsStack.addAll(items);
				while ( ! itemsStack.isEmpty() ) {
					CodeListItem item = itemsStack.pop();
					if ( isVersionInUse(version, item) ) {
						references.add(item);
					}
					itemsStack.addAll(item.getChildItems());
				}
			}
		}
		return references;
	}
	
	protected boolean isVersionInUse(ModelVersion version, VersionableSurveyObject object) {
		ModelVersion sinceVersion = object.getSinceVersion();
		ModelVersion deprecatedVersion = object.getDeprecatedVersion();
		return version.equals(sinceVersion) || version.equals(deprecatedVersion);
	}
	
	private void dispatchVersionsUpdatedCommand() {
		BindUtils.postGlobalCommand(null, null, VERSIONS_UPDATED_GLOBAL_COMMAND, null);
	}

	@Override
	protected void dispatchChangesAppliedCommand(boolean ignoreUnsavedChanges) {
		postCloseVersioningManagerPopUpCommand();
	}
	
	@Override
	protected void dispatchChangesCancelledCommand() {
		postCloseVersioningManagerPopUpCommand();
	}
	
	private void postCloseVersioningManagerPopUpCommand() {
		BindUtils.postGlobalCommand(null, null, "closeVersioningManagerPopUp", null);
	}
}
