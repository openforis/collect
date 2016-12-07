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
		COLLECT_EARTH_AREA_PER_ATTRIBUTE("ce_area", "areas_per_attribute.csv"),
		COLLECT_EARTH_GRID("ce_grid"),
		COLLECT_EARTH_EE_SCRIPT("ce_eescript", "eePlaygroundScript.fmt"),
		COLLECT_EARTH_SAIKU_QUERY("ce_saiku_query"),
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
		private String fixedFilename;
		
		private SurveyFileType(String code) {
			this(code, null);
		}
		
		private SurveyFileType(String code, String fixedFilename) {
			this.code = code;
			this.fixedFilename = fixedFilename;
		}
		
		public String getCode() {
			return code;
		}
		
		public String getFixedFilename() {
			return fixedFilename;
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
