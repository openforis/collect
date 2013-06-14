/**
 * 
 */
package org.openforis.collect.model;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class SurveySummary {

	private Integer id;
	private Integer publishedId;
	private String name;
	private String uri;
	private String projectName;
	private boolean work;
	private boolean published;
	
	public SurveySummary(Integer id, String name, String uri) {
		this(id, name, uri, null);
	}
	
	public SurveySummary(Integer id, String name, String uri, String projectName) {
		super();
		this.id = id;
		this.name = name;
		this.uri = uri;
		this.projectName = projectName;
		this.published = true;
		this.work = false;
	}
	
	public static SurveySummary createFromSurvey(CollectSurvey survey) {
		return createFromSurvey(survey, null);
	}
	
	public static SurveySummary createFromSurvey(CollectSurvey survey, String lang) {
		Integer id = survey.getId();
		String projectName = survey.getProjectName(lang);
		String name = survey.getName();
		String uri = survey.getUri();
		SurveySummary summary = new SurveySummary(id, name, uri, projectName);
		summary.setWork(survey.isWork());
		return summary;
	}

	public Integer getId() {
		return id;
	}
	
	public boolean isWork() {
		return work;
	}

	public void setWork(boolean work) {
		this.work = work;
	}
	
	public String getName() {
		return name;
	}

	public String getUri() {
		return uri;
	}
	
	public String getProjectName() {
		return projectName;
	}

	public boolean isPublished() {
		return published;
	}
	
	public void setPublished(boolean published) {
		this.published = published;
	}
	
	public Integer getPublishedId() {
		return publishedId;
	}
	
	public void setPublishedId(Integer publishedId) {
		this.publishedId = publishedId;
	}
}
