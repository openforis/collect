package org.openforis.collect.io.metadata.collectearth.balloon;

import java.util.List;


/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
class CEEnumeratedEntity extends CEEntity {

	private List<String> enumeratingCodes;

	public CEEnumeratedEntity(String name, String label, boolean multiple, List<String> enumeratingCodes) {
		super(name, label, multiple);
		this.enumeratingCodes = enumeratingCodes;
	}

	public List<String> getEnumeratingCodes() {
		return enumeratingCodes;
	}
	
}