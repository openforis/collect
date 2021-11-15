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

	private String type;
	private String filenames;

	@Override
	public void loadFrom(SurveyFile source, String language) {
		type = source.getType().name();
		filenames = source.getFilename();
	}
	
	@Override
	public void saveTo(SurveyFile dest, String language) {
		dest.setType(SurveyFileType.valueOf(type));
		dest.setFilename(filenames);
	}
	
	@Override
	protected void reset() {
		type = SurveyFileType.GENERIC.name();
		filenames = "";
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
	
}
