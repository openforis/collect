/**
 * 
 */
package org.openforis.collect.model;

/**
 * @author M. Togna
 * 
 */
public class SurveySummary {

	private Integer id;
	private String uri;
	private String projectName;

	public SurveySummary(Integer id, String uri, String projectName) {
		super();
		this.id = id;
		this.uri = uri;
		this.projectName = projectName;
	}

	public Integer getId() {
		return id;
	}

	public String getUri() {
		return uri;
	}

	public String getProjectName() {
		return projectName;
	}

}
