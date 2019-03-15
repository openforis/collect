package org.openforis.collect.event;

import java.util.Locale;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public enum RecordStep {

	ENTRY, CLEANSING, ANALYSIS;
	
	public String nameLowerCase() {
		return name().toLowerCase(Locale.ENGLISH);
	}
}
