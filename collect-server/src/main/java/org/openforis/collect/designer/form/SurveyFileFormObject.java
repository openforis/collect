package org.openforis.collect.designer.form;

import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyFileFormObject extends FormObject<SurveyFile> {
	
	private String type;
	private String filename;

	@Override
	public void loadFrom(SurveyFile source, String language) {
		type = source.getType().name();
		filename = source.getFilename();
	}
	
	@Override
	public void saveTo(SurveyFile dest, String language) {
		dest.setType(SurveyFileType.valueOf(type));
		dest.setFilename(filename);
	}
	
	@Override
	protected void reset() {
		type = SurveyFileType.GENERIC.name();
		filename = null;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
}
