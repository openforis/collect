/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.SurveyFileFormObject;
import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;
import org.openforis.commons.io.OpenForisIOUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.SimpleForm;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 * @author S. Ricci
 *
 */
public class SurveyFileVM extends SurveyObjectBaseVM<SurveyFile> {

	private static final String APPLY_CHANGES_TO_EDITED_SURVEY_FILE_GLOBAL_COMMAND = "applyChangesToEditedSurveyFile";
	private static final String CLOSE_SURVEY_FILE_EDIT_POPUP_GLOBAL_COMMAND = "closeSurveyFileEditPopUp";
	
	@WireVariable
	private SurveyManager surveyManager;
	
	private File uploadedFile;
	private String uploadedFileName;

	private Map<String, String> form = new HashMap<String, String>();

	public SurveyFileVM() {
		setCommitChangesOnApply(false);
		fieldLabelKeyPrefixes.addAll(0, Arrays.asList("survey.schema.attribute.attribute_default"));
	}
	
	@Init(superclass=false)
	public void init(@ContextParam(ContextType.BINDER) Binder binder, 
			@ExecutionArgParam("surveyFile") SurveyFile surveyFile, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init();
		this.newItem = newItem;
		setEditedItem(surveyFile);
		if (surveyFile != null && ! newItem) {
			validateForm(binder);
		}
	}

	@Override
	protected FormObject<SurveyFile> createFormObject() {
		return new SurveyFileFormObject();
	}

	@Override
	protected List<SurveyFile> getItemsInternal() {
		return null;
	}

	@Override
	protected SurveyFile createItemInstance() {
		return null;
	}

	@Override
	protected void addNewItemToSurvey() {
		surveyManager.addSurveyFile(survey, editedItem, uploadedFile);
	}

	@Override
	protected void deleteItemFromSurvey(SurveyFile item) {
	}
	
	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {
	}
	
	@Command
	public void close() {
		BindUtils.postGlobalCommand(null, null, CLOSE_SURVEY_FILE_EDIT_POPUP_GLOBAL_COMMAND, null);
	}
	
	@Override
	@Command
	public void commitChanges(@ContextParam(ContextType.BINDER) Binder binder) {
		dispatchApplyChangesCommand(binder);
		if ( checkCanLeaveForm() ) {
			if (newItem && uploadedFile == null) {
				MessageUtil.showError("global.file_not_selected");
			} else {
				boolean wasNewItem = newItem;
				super.commitChanges(binder);
				if (! wasNewItem) {
					surveyManager.updateSurveyFile(survey, editedItem, uploadedFile);
				}
				BindUtils.postGlobalCommand(null, null, APPLY_CHANGES_TO_EDITED_SURVEY_FILE_GLOBAL_COMMAND, null);
			}
		}
	}
	
	@Command
	public void fileUploaded(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event,
			@ContextParam(ContextType.BINDER) Binder binder) {
 		Media media = event.getMedia();
		String fileName = media.getName();
		File tempFile;
		if (media.isBinary()) {
			tempFile = OpenForisIOUtils.copyToTempFile(media.getStreamData(), FilenameUtils.getExtension(fileName));
		} else {
			tempFile = OpenForisIOUtils.copyToTempFile(media.getReaderData(), FilenameUtils.getExtension(fileName));
		}
		this.uploadedFile = tempFile;
		this.uploadedFileName = normalizeFilename(fileName);
		notifyChange(SurveyFileFormObject.UPLOADED_FILE_NAME_FIELD);
		updateForm(binder);
	}
	
	@Command
	public void typeChanged(@ContextParam(ContextType.BINDER) Binder binder) {
		updateForm(binder);
	}
	
	private void updateForm(Binder binder) {
		SimpleForm tempForm = ComponentUtil.getForm(binder.getView());
		String typeName = (String) tempForm.getField("type");
		SurveyFileType type = SurveyFileType.valueOf(typeName);
		String filename = type.getFixedFilename();
		if (filename == null) {
			if (uploadedFile == null) {
				filename = (String) tempForm.getField(SurveyFileFormObject.FILENAME_FIELD_NAME);
			} else {
				filename = uploadedFileName;
			}
		}
		setValueOnFormField(tempForm, SurveyFileFormObject.FILENAME_FIELD_NAME, filename);
		dispatchApplyChangesCommand(binder);
	}

	private String normalizeFilename(String filename) {
		return filename.replaceAll("[^\\w-.]", "_");
	}

	public String getUploadedFileName() {
		return uploadedFileName;
	}
	
	public Map<String, String> getForm() {
		return form ;
	}
	
	public void setForm(Map<String, String> form) {
		this.form = form;
	}
	
	public SurveyManager getSurveyManager() {
		return surveyManager;
	}
}
