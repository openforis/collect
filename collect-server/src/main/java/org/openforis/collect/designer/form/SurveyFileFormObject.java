package org.openforis.collect.designer.form;

import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyFileFormObject extends FormObject<SurveyFile> {
	
	public static final String FILENAMES_FIELD_NAME = "filenames";
	public static final String TYPE_FIELD_NAME = "type";
	public static final String MULTIPLE_FILES_UPLOADED_FIELD_NAME = "multipleFilesUploaded";

	private String type;
	private String filenames;
	private boolean multipleFilesUploaded;

	@Override
	public void loadFrom(SurveyFile source, String language) {
		type = source.getType().name();
		filenames = source.getFilename();
		multipleFilesUploaded = false;
	}
	
	@Override
	public void saveTo(SurveyFile dest, String language) {
		if (multipleFilesUploaded) {
		} else {
			dest.setType(SurveyFileType.valueOf(type));
			dest.setFilename(filenames);
		}
	}
	
	@Override
	protected void reset() {
		type = SurveyFileType.GENERIC.name();
		filenames = "";
		multipleFilesUploaded = false;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFilenames() {
		return filenames;
	}
	
	public void setFilenames(String filenames) {
		this.filenames = filenames;
	}
	
	public boolean isMultipleFilesUploaded() {
		return multipleFilesUploaded;
	}
	
	public void setMultipleFilesUploaded(boolean multipleFilesUploaded) {
		this.multipleFilesUploaded = multipleFilesUploaded;
	}
	
}
