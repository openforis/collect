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
	private String projectName;

	public SurveySummary(Integer id, String name, String projectName) {
		super();
		this.id = id;
		this.name = name;
		this.projectName = projectName;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getProjectName() {
		return projectName;
	}

}
