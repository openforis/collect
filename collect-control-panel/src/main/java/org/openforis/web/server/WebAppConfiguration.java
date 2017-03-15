package org.openforis.web.server;

public class WebAppConfiguration {
	
	private String warFileLocation;
	private String context;
	
	public WebAppConfiguration(String warFileLocation, String context) {
		super();
		this.warFileLocation = warFileLocation;
		this.context = context;
	}
	
	public String getWarFileLocation() {
		return warFileLocation;
	}
	
	public String getContext() {
		return context;
	}
}