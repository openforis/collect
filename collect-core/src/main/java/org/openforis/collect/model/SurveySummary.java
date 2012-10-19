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
	private String name;
	private String uri;
	private String projectName;

	public SurveySummary(Integer id, String name, String uri) {
		this(id, name, uri, null);
	}

	public SurveySummary(Integer id, String name, String uri, String projectName) {
		super();
		this.id = id;
		this.name = name;
		this.uri = uri;
		this.projectName = projectName;
	}

	public Integer getId() {
		return id;
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

}
