/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.SurveyFileFormObject;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.SurveyFile;
import org.openforis.commons.io.OpenForisIOUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
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
	
	@WireVariable
	private SurveyManager surveyManager;
	
	private File uploadedFile;
	private String uploadedFileName;

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
		survey.addFile(editedItem);
	}

	@Override
	protected void deleteItemFromSurvey(SurveyFile item) {
		survey.removeFile(item.getFilename());
	}
	
	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {
	}
	
	@Override
	@Command
	public void commitChanges(@ContextParam(ContextType.BINDER) Binder binder) {
		dispatchApplyChangesCommand(binder);
		if ( checkCanLeaveForm() ) {
			super.commitChanges(binder);
			if (newItem) {
				surveyManager.addSurveyFile(survey, editedItem, uploadedFile);
			} else {
				surveyManager.updateSurveyFile(survey, editedItem, uploadedFile);
			}
			BindUtils.postGlobalCommand(null, null, APPLY_CHANGES_TO_EDITED_SURVEY_FILE_GLOBAL_COMMAND, null);
		}
	}
	
	@Command
	public void fileUploaded(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event) {
 		Media media = event.getMedia();
		String fileName = media.getName();
		File tempFile;
		if (media.isBinary()) {
			tempFile = OpenForisIOUtils.copyToTempFile(media.getReaderData(), FilenameUtils.getExtension(fileName));
		} else {
			tempFile = OpenForisIOUtils.copyToTempFile(media.getReaderData(), FilenameUtils.getExtension(fileName));
		}
		this.uploadedFile = tempFile;
		this.uploadedFileName = fileName;
		notifyChange("uploadedFileName");
	}
	
	public String getUploadedFileName() {
		return uploadedFileName;
	}
	
}
