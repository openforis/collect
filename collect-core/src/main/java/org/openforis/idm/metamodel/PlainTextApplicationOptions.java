package org.openforis.idm.metamodel;


/**
 * @author G. Miceli
 */
public class PlainTextApplicationOptions implements ApplicationOptions {
	private String type;
	private String body;
	
	public PlainTextApplicationOptions() {
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
