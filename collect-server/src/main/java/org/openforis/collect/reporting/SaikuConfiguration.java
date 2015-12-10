package org.openforis.collect.reporting;

/**
 * 
 * @author S. Ricci
 *
 */
public class SaikuConfiguration {

	private static SaikuConfiguration instance;
	
	private String contextPath = "saiku";
	
	private SaikuConfiguration() {
		
	}
	
	public static SaikuConfiguration getInstance() {
		if (instance == null) {
			instance = new SaikuConfiguration();
		}
		return instance;
	}
	
	public String getContextPath() {
		return contextPath;
	}
	
}
