package org.openforis.collect.model;

import org.openforis.collect.persistence.jooq.tables.pojos.OfcImagery;
import org.openforis.idm.metamodel.PersistedObject;

public class Imagery extends OfcImagery implements PersistedObject {

	private static final long serialVersionUID = 1L;

	public enum Visibility {
		PUBLIC('P'), PRIVATE('N');
		
		private char code;
		
		Visibility(char code) {
			this.code = code;
		}
		
		public char getCode() {
			return code;
		}

		public static Visibility fromCode(String code) {
			if (code == null) {
				return null;
			} else {
				return fromCode(code.charAt(0));
			}
		}

		private static Visibility fromCode(char charCode) {
			Visibility[] values = values();
			for (Visibility v : values) {
				if (v.code == charCode) {
					return v;
				}
			}
			return null;
		}
	}
	
	public Imagery() {}
	
	public Imagery(OfcImagery imagery) {
		super(imagery);
	}
	
	public Visibility getVisibilityEnum() {
		return Visibility.fromCode(this.getVisibility());
	}

}
