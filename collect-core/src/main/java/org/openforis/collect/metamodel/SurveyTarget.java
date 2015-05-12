package org.openforis.collect.metamodel;

/**
 * 
 * @author S. Ricci
 *
 */
public enum SurveyTarget {
	
	COLLECT_DESKTOP("CD"), 
	COLLECT_EARTH("CE");
	
	private String code;

	SurveyTarget(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public static SurveyTarget fromCode(String code) {
		for (SurveyTarget target : values()) {
			if (target.getCode().equals(code)) {
				return target;
			}
		}
		throw new IllegalArgumentException("Not a valid survey type code: " + code);
	}
}