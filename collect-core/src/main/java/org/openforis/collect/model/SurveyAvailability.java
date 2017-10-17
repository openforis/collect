package org.openforis.collect.model;

public enum SurveyAvailability {
	UNPUBLISHED('u'), PUBLISHED('p'), CLOSED('c'), ARCHIVED('a');
	
	private char code;

	SurveyAvailability(char code) {
		this.code = code;
	}
	
	public static SurveyAvailability fromCode(char code) {
		for (SurveyAvailability val : values()) {
			if (val.code == code) {
				return val;
			}
		}
		throw new IllegalArgumentException("Invalid code for CollectSurvey.Visibility: " + code);
	}
	
	public char getCode() {
		return code;
	}
}