package org.openforis.collect.designer.viewmodel;

import java.io.File;

import org.openforis.collect.designer.util.MediaUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class BaseSurveyFileImportVM extends SurveyBaseVM {

	protected File uploadedFile;
	protected String uploadedFileName;
	
	public BaseSurveyFileImportVM() {
		reset();
	}
	
	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
	}

	protected void reset() {
		if ( uploadedFile != null ) {
			uploadedFile.delete();
			uploadedFile = null;
		}
		uploadedFileName = null;
		notifyChange("uploadedFileName");
	}
	
	@Command
	public void fileUploaded(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event) {
 		Media media = event.getMedia();
 		checkCanImportFile(media);
		this.uploadedFile = MediaUtil.copyToTempFile(media);
		this.uploadedFileName = media.getName();
		notifyChange("uploadedFileName");
	}
	
	protected abstract void checkCanImportFile(Media media);

	protected boolean validateForm(BindContext ctx) {
		String messageKey = null;
		if ( uploadedFile == null ) {
			messageKey = "global.file_not_selected";
		}
		if ( messageKey == null ) {
			return true;
		} else {
			MessageUtil.showWarning(messageKey);
			return false;
		}
	}
	
	public String getUploadedFileName() {
		return uploadedFileName;
	}
	
}
