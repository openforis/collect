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
	private boolean work;
	private String name;
	private String uri;
	private String projectName;

	public SurveySummary(Integer id, String name, String uri) {
		this(id, false, name, uri, null);
	}

	public SurveySummary(Integer id, boolean work, String name, String uri) {
		this(id, work, name, uri, null);
	}

	public SurveySummary(Integer id, String name, String uri, String projectName) {
		this(id, false, name, uri, projectName);
	}
	
	public SurveySummary(Integer id, boolean work, String name, String uri, String projectName) {
		super();
		this.id = id;
		this.work = work;
		this.name = name;
		this.uri = uri;
		this.projectName = projectName;
	}

	public Integer getId() {
		return id;
	}
	
	public boolean isWork() {
		return work;
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
