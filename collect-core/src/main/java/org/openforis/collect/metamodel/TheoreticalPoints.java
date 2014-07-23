package org.openforis.collect.metamodel;

import java.util.List;

import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class TheoreticalPoints {

	private List<String> infoNames;
	
	public List<String> getInfoNames() {
		return CollectionUtils.unmodifiableList(infoNames);
	}
	
	public void setInfoNames(List<String> infoNames) {
		this.infoNames = infoNames;
	}
	
}
