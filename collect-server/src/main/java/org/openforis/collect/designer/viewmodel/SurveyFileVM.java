/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.SurveyFileFormObject;
import org.openforis.collect.designer.util.MediaUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.io.metadata.collectearth.CSVFileValidationResult;
import org.openforis.collect.io.metadata.collectearth.CollectEarthGridTemplateGenerator;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.validation.SurveyValidator.ValidationParameters;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.MediaTypes;
import org.openforis.commons.collection.CollectionUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Listitem;

/**
 * @author S. Ricci
 *
 */
public class SurveyFileVM extends SurveyObjectBaseVM<SurveyFile> {

	private static final String APPLY_CHANGES_TO_EDITED_SURVEY_FILE_GLOBAL_COMMAND = "applyChangesToEditedSurveyFile";
	private static final String CLOSE_SURVEY_FILE_EDIT_POPUP_GLOBAL_COMMAND = "closeSurveyFileEditPopUp";
	
	@WireVariable
	private SurveyManager surveyManager;
	
	private List<File> uploadedFiles;
	private List<String> uploadedFileNames;

	private Map<String, String> form = new HashMap<String, String>();
	private Set<String> selectedUploadedFileNames = new HashSet<>();

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
		if (isMultipleFilesUploaded()) {
			List<SurveyFile> surveyFiles = new ArrayList<>(uploadedFiles.size());
			for (String filename : uploadedFileNames) {
				SurveyFile surveyFile = new SurveyFile(survey);
				surveyFile.setType(editedItem.getType());
				surveyFile.setFilename(filename);
				surveyFiles.add(surveyFile);
			}
			surveyManager.addSurveyFiles(survey, surveyFiles, uploadedFiles);
		} else {
			surveyManager.addSurveyFile(survey, editedItem, uploadedFiles.get(0));			
		}
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
			if (newItem && uploadedFiles == null || uploadedFiles.isEmpty()) {
				MessageUtil.showError("global.file_not_selected");
			} else {
				if (uploadedFiles == null || validateFileContent(binder)) {
					boolean wasNewItem = newItem;
					super.commitChanges(binder);
					if (! wasNewItem) {
//						surveyManager.updateSurveyFile(survey, editedItem, uploadedFile);
					}
					BindUtils.postGlobalCommand(null, null, APPLY_CHANGES_TO_EDITED_SURVEY_FILE_GLOBAL_COMMAND, null);
				}
			}
		}
	}

	private boolean validateFileContent(Binder binder) {
		String typeName = getFormFieldValue(binder, SurveyFileFormObject.TYPE_FIELD_NAME);
		SurveyFileType type = SurveyFileType.valueOf(typeName);
		switch (type) {
			case COLLECT_EARTH_GRID:
				CollectEarthGridTemplateGenerator templateGenerator = new CollectEarthGridTemplateGenerator();
				for (File uploadedFile : uploadedFiles) {
					CSVFileValidationResult headersValidationResult = templateGenerator.validate(uploadedFile, survey, new ValidationParameters() );
					if (!headersValidationResult.isSuccessful()) {
						switch(headersValidationResult.getErrorType()) {
						case INVALID_FILE_TYPE:
							MessageUtil.showWarning("survey.file.error.invalid_file_type", "CSV (Comma Separated Values)");
							break;
						case INVALID_HEADERS:
							MessageUtil.showWarning("survey.file.type.collect_earth_grid.error.invalid_file_structure", 
									new Object[]{headersValidationResult.getExpectedHeaders().toString(), 
											headersValidationResult.getFoundHeaders().toString()});
							break;
						case INVALID_NUMBER_OF_PLOTS_WARNING:
							MessageUtil.showWarning("survey.file.error.warning_csv_size",  CollectEarthGridTemplateGenerator.CSV_LENGTH_WARNING +"");
							break;
						case INVALID_NUMBER_OF_PLOTS_TOO_LARGE:
							MessageUtil.showWarning("survey.file.error.error_csv_size",  CollectEarthGridTemplateGenerator.CSV_LENGTH_ERROR +"");
							//block the user , a file so large would make the CEP file unusable!
							return false; 
						default:
						}
					}
				}
				return true;
			default:
				return true;
		}
	}
	
	@Command
	public void fileUploaded(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event,
			@ContextParam(ContextType.BINDER) Binder binder) {
 		Media[] medias = event.getMedias();
 		if (medias.length > 1 && !newItem) {
 			MessageUtil.showWarning("survey.file.error.multiple_files_allowed_only_for_new_survey_file");
 			return;
 		}
 		this.uploadedFiles = new ArrayList<>();
 		this.uploadedFileNames = new ArrayList<>();
 		this.selectedUploadedFileNames = new HashSet<>();
 		for (Media media : medias) {
 			File uploadedFile = MediaUtil.copyToTempFile(media);
 			this.uploadedFiles.add(uploadedFile);
 			this.uploadedFileNames.add(normalizeFilename(media.getName()));
		}
 		updateForm(binder);
 		notifyChange("uploadedFileName", "uploadedFileNames", "multipleFilesUploaded", "selectedUploadedFileName");
	}
	
	@Command
	public void typeChanged(@ContextParam(ContextType.BINDER) Binder binder) {
		updateForm(binder);
	}
	
	@Command
	public void downloadExampleFile(@BindingParam("fileType") String fileType) throws IOException {
		switch (SurveyFileType.valueOf(fileType)) {
		case COLLECT_EARTH_GRID:
			File templateFile = new CollectEarthGridTemplateGenerator().generateTemplateCSVFile(survey);
			String fileName = String.format("%s_grid_template_%s.csv", survey.getName(), Dates.formatDateTime(new Date()));
			Filedownload.save(new FileInputStream(templateFile), MediaTypes.CSV_CONTENT_TYPE, fileName);
			break;
		default:
			//TODO
		}
	}
	
	@Command
	public void downloadUploadedFile() throws IOException {
		if (uploadedFiles != null && uploadedFiles.size() == 1) {
			String fileName = uploadedFileNames.get(0);
			String contentType = URLConnection.guessContentTypeFromName(fileName);
			Filedownload.save(new FileInputStream(uploadedFiles.get(0)), contentType, fileName);
		}
	}
	
	@Command
	public void uploadedFileNamesSelected(@BindingParam("filenames") Set<Listitem> filenameItems) {
		Set<String> fileNames = new HashSet<>();;
		for (Listitem listitem : filenameItems) {
			fileNames.add(listitem.getValue());
		}
		this.selectedUploadedFileNames = fileNames;
		notifyChange("selectedUploadedFileNames");
	}
	
	@Command
	public void deleteUploadedFile(@BindingParam("filename") String filename) {
		_deleteSelectedUploadedFilename(filename);
		selectedUploadedFileNames.remove(filename);
		notifyChange("uploadedFiles", "uploadedFileNames", "selectedUploadedFileNames");
	}
	
	@Command
	public void deleteSelectedUploadedFiles() {
		for (String selectedUploadedFileName : selectedUploadedFileNames) {
			_deleteSelectedUploadedFilename(selectedUploadedFileName);
		}
		selectedUploadedFileNames = null;
		notifyChange("uploadedFiles", "uploadedFileNames", "selectedUploadedFileNames");
	}

	private void _deleteSelectedUploadedFilename(String selectedUploadedFileName) {
		int index = uploadedFileNames.indexOf(selectedUploadedFileName);
		uploadedFiles.remove(index);
		uploadedFileNames.remove(index);
	}
	
	@Command
	public void downloadFile() {
		byte[] content = surveyManager.loadSurveyFileContent(editedItem);
		String fileName = editedItem.getFilename();
		String contentType = URLConnection.guessContentTypeFromName(fileName);
		Filedownload.save(content, contentType, fileName);
	}
	
	private void updateForm(Binder binder) {
		String filename = null;
		if (isMultipleFilesUploaded()) {
			filename = getUploadedFileName();
		} else {
			String typeName = getFormFieldValue(binder, SurveyFileFormObject.TYPE_FIELD_NAME);
			SurveyFileType type = SurveyFileType.valueOf(typeName);
			filename = type.getFixedFilename();
			if (filename == null) {
				if (CollectionUtils.isNotEmpty(uploadedFiles)) {
					filename = getUploadedFileName();
				} else {
					filename = getFormFieldValue(binder, SurveyFileFormObject.FILENAMES_FIELD_NAME);
				}
			}
		}
		setFormFieldValue(binder, SurveyFileFormObject.FILENAMES_FIELD_NAME, filename);
		dispatchApplyChangesCommand(binder);
	}

	private String normalizeFilename(String filename) {
		return filename.replaceAll("[^\\w-.]", "_");
	}

	public String getEditedItemFilename() {
		return editedItem == null ? null : editedItem.getFilename();
	}
	
	public String getUploadedFileName() {
		return uploadedFileNames == null ? null : StringUtils.join(uploadedFileNames, "\n");
	}
	
	public List<String> getUploadedFileNames() {
		return uploadedFileNames;
	}
	
	public Set<String> getSelectedUploadedFileNames() {
		return selectedUploadedFileNames;
	}
	
	public boolean isMultipleFilesUploaded() {
		return uploadedFiles != null && uploadedFiles.size() > 1;
	}
	
	public Map<String, String> getForm() {
		return form;
	}
	
	public void setForm(Map<String, String> form) {
		this.form = form;
	}
	
	public SurveyManager getSurveyManager() {
		return surveyManager;
	}
}
