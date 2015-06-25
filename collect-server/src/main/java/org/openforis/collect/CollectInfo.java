package org.openforis.collect;

import org.openforis.commons.versioning.Version;


/**
 * 
 * @author S. Ricci
 *
 */
public class CollectInfo {
	
	private String version;
	
	public CollectInfo() {
		this(Collect.VERSION);
	}
	
	public CollectInfo(Version version) {
		this(version.toString());
	}
	
	public CollectInfo(String version) {
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
}