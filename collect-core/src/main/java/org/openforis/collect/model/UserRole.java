package org.openforis.collect.model;

public enum UserRole {
	
	ENTRY("ROLE_ENTRY"),
	CLEANSING("ROLE_CLEANSING"),
	ANALYSIS("ROLE_ANALYSIS"),
	ADMIN("ROLE_ADMIN");
	
	private String code;

	UserRole(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
}