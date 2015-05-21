package org.openforis.collect.io.parsing;

import org.openforis.commons.io.OpenForisIOUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public enum FileCharset {
	
	UTF_8(OpenForisIOUtils.UTF_8), 
	WESTERN_EUROPEAN("ISO-8859-1");
	
	private String code;

	private FileCharset(String code) {
		this.code = code;
	}
	
	public String getCharsetName() {
		return code;
	}

}
