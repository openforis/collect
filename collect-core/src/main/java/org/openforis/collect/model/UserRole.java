package org.openforis.collect.model;

public enum UserRole {
	
	ENTRY("ROLE_ENTRY", 1),
	CLEANSING("ROLE_CLEANSING", 2),
	ANALYSIS("ROLE_ANALYSIS", 3),
	ADMIN("ROLE_ADMIN", 4);
	
	public static UserRole fromCode(String code) {
		for (UserRole role : values()) {
			if ( role.getCode().equals(code) ) {
				return role;
			}
		}
		throw new IllegalArgumentException(code + " is not a valid UserRole code");
	}

	private String code;
	private int hierarchicalOrder;

	UserRole(String code, int hierarchicalOrder) {
		this.code = code;
		this.hierarchicalOrder = hierarchicalOrder;
	}
	
	public String getCode() {
		return code;
	}
	
	public int getHierarchicalOrder() {
		return hierarchicalOrder;
	}

}