package org.openforis.collect.model;

import org.openforis.idm.metamodel.PersistedSurveyObject;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyFile extends PersistedSurveyObject {
	
	private static final long serialVersionUID = 1L;

	public enum SurveyFileType {
		COLLECT_EARTH_AREA_PER_ATTRIBUTE("ce_area"),
		GENERIC("generic");
		
		public static SurveyFileType fromCode(String code) {
			SurveyFileType[] values = values();
			for (SurveyFileType surveyFileType : values) {
				if (code.equals(surveyFileType.getCode())) {
					return surveyFileType;
				}
			}
			throw new IllegalArgumentException("Invalid code for SurveyFileType: " + code);
		}
		
		private String code;
		
		private SurveyFileType(String code) {
			this.code = code;
		}
		
		public String getCode() {
			return code;
		}
	}
	
	private SurveyFileType type;
	private String filename;
	
	public SurveyFile(CollectSurvey survey) {
		super(survey);
		this.type = SurveyFileType.GENERIC;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public SurveyFileType getType() {
		return type;
	}

	public void setType(SurveyFileType type) {
		this.type = type;
	}
	
}
