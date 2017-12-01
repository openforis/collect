package org.openforis.collect.model;

public enum UserRole {
	
	VIEW(UserRoles.VIEW, 0),
	ENTRY_LIMITED(UserRoles.ENTRY_LIMITED, 1),
	ENTRY(UserRoles.ENTRY, 2),
	CLEANSING(UserRoles.CLEANSING, 3),
	ANALYSIS(UserRoles.ANALYSIS, 4),
	ADMIN(UserRoles.ADMIN, 5);
	
	
	public static UserRole fromCode(String code) {
		for (UserRole role : values()) {
			if ( role.getCode().equals(code) ) {
				return role;
			}
		}
		throw new IllegalArgumentException(code + " is not a valid UserRole code");
	}

	public static UserRole fromHierarchicalOrder(int hierarchicalOrder) {
		for (UserRole role : values()) {
			if (role.getHierarchicalOrder() == hierarchicalOrder) {
				return role;
			}
		}
		throw new IllegalArgumentException(hierarchicalOrder + " is not a valid UserRole hieararchical order");
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