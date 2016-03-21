package org.openforis.collect.model;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyFile  {
	
	public enum SurveyFileType {
		COLLECT_EARTH_AREA_PER_ATTRIBUTE,
		GENERIC
	}
	
	private Integer id;
	private CollectSurvey survey;
	private SurveyFileType type;
	private String filename;
	
	public SurveyFile(CollectSurvey survey) {
		this.type = SurveyFileType.GENERIC;
		this.survey = survey;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SurveyFile other = (SurveyFile) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
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
