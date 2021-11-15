/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.SurveyMainInfoFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyMainInfoVM extends SurveyObjectBaseVM<CollectSurvey> {
	
	@WireVariable
	private SurveyManager surveyManager;
	
	private boolean editingNewSurveyFile;
	private SurveyFile editedSurveyFile;
	private Set<SurveyFile> selectedSurveyFiles = new HashSet<SurveyFile>();

	private Window surveyFilePopUp;
	
	@Init(superclass=false)
	public void init(@ContextParam(ContextType.BINDER) Binder binder) {
		super.init();
		setEditedItem(getSurvey());
		validateForm(binder);
	}
	
	@Override
	protected void performItemSelection(CollectSurvey item) {
		super.performItemSelection(item);
		dispatchValidateAllCommand();
	}
	
	@Override
	protected CollectSurvey createItemInstance() {
		//do nothing, no child instances created
		return null;
	}
	
	@Override
	protected FormObject<CollectSurvey> createFormObject() {
		return new SurveyMainInfoFormObject();
	}

	@Override
	protected List<CollectSurvey> getItemsInternal() {
		return null;
	}
	
	@Override
	protected void addNewItemToSurvey() {}

	@Override
	protected void deleteItemFromSurvey(CollectSurvey item) {}
	
	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {}

	
	public SurveyManager getSurveyManager() {
		return surveyManager;
	}
	
	public Integer getEditedSurveyPublishedId() {
		return getSessionStatus().getPublishedSurveyId();
	}
	
	public String getSurveyFileTypeLabel(SurveyFile surveyFile) {
		SurveyFileType type = surveyFile.getType();
		return Labels.getLabel("survey.file.type." + type.name().toLowerCase(Locale.ENGLISH));
	}
	
	public String getSurveyFileName(SurveyFile surveyFile) {
		return surveyFile.getFilename();
	}
	
	public List<SurveyFile> getSurveyFiles() {
		return survey == null ? null : surveyManager.loadSurveyFileSummaries(survey);
	}
	
	@Command
	public void addSurveyFile() {
		editedSurveyFile = new SurveyFile(survey);
		editingNewSurveyFile = true;
		openSurveyFileEditPopUp();
	}
	
	@Command
	public void editSelectedSurveyFile() {
		if (!isSingleSurveyFileSelected()) {
			return;
		}
		editedSurveyFile = selectedSurveyFiles.iterator().next();
		editingNewSurveyFile = false;
		openSurveyFileEditPopUp();
	}
	
	@Command
	public void downloadSelectedSurveyFile() {
		if (!isSingleSurveyFileSelected()) {
			return;
		}
		SurveyFile selectedSurveyFile = selectedSurveyFiles.iterator().next();
		byte[] content = surveyManager.loadSurveyFileContent(selectedSurveyFile);
		String fileName = selectedSurveyFile.getFilename();
		String contentType = URLConnection.guessContentTypeFromName(fileName);
		Filedownload.save(content, contentType, fileName);
	}
	
	@GlobalCommand
	public void applyChangesToEditedSurveyFile(@ContextParam(ContextType.BINDER) Binder binder) {
		closeSurveyFileEditPopUp(binder);
		notifyChange("surveyFiles");
	}
	
	private void openSurveyFileEditPopUp() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("newItem", editingNewSurveyFile);
		args.put("surveyFile", editedSurveyFile);
		surveyFilePopUp = openPopUp(Resources.Component.SURVEY_FILE_POPUP.getLocation(), true, args);
	}
	
	@GlobalCommand
	public void closeSurveyFileEditPopUp(@ContextParam(ContextType.BINDER) Binder binder) {
		closePopUp(surveyFilePopUp);
		surveyFilePopUp = null;
		validateForm(binder);
	}
	
	@Command
	public void deleteSelectedSurveyFiles() {
		if (selectedSurveyFiles.isEmpty()) {
			return;
		}
		String messageKey = isSingleSurveyFileSelected() ? 
				"survey.file.delete.confirm" :
				"survey.file.delete_multiple.confirm";
		String[] messageArgs = isSingleSurveyFileSelected() ?
				new String[]{selectedSurveyFiles.iterator().next().getFilename()}:
				new String[]{Integer.toString(selectedSurveyFiles.size())};
		MessageUtil.showConfirm(new ConfirmHandler() {
			@Override
			public void onOk() {
				surveyManager.deleteSurveyFiles(selectedSurveyFiles);
				setSelectedSurveyFiles(new HashSet<>()); 
				notifyChange("surveyFiles", "selectedSurveyFiles");
			}
		}, messageKey, messageArgs);
	}
	
	public boolean isSingleSurveyFileSelected() {
		return selectedSurveyFiles.size() == 1;
	}
	
	public Set<SurveyFile> getSelectedSurveyFiles() {
		return selectedSurveyFiles;
	}
	
	public void setSelectedSurveyFiles(Set<SurveyFile> selectedSurveyFiles) {
		this.selectedSurveyFiles = selectedSurveyFiles;
		notifyChange("singleSurveyFileSelected");
	}
}
