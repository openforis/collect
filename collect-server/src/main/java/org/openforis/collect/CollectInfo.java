package org.openforis.collect;

import org.openforis.commons.versioning.Version;


/**
 * 
 * @author S. Ricci
 *
 */
public class CollectInfo {
	
	private String version;
	private boolean latestReleaseVersionVerified;
	private boolean latestReleaseVersion;
	
	public CollectInfo() {
		this(Collect.VERSION);
	}
	
	public CollectInfo(Version version) {
		this(version, null);
	}

	public CollectInfo(Version version, Version latestReleaseVersion) {
		this(version.toString(), latestReleaseVersion != null, 
				latestReleaseVersion == null ? false : version.compareTo(latestReleaseVersion) >= 0);
	}

	public CollectInfo(String version, boolean latestVersionVerified, boolean latestVersion) {
		this.version = version;
		this.latestReleaseVersionVerified = latestVersionVerified;
		this.latestReleaseVersion = latestVersion;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public boolean isLatestReleaseVersionVerified() {
		return latestReleaseVersionVerified;
	}
	
	public void setLatestReleaseVersionVerified(boolean latestReleaseVersionVerified) {
		this.latestReleaseVersionVerified = latestReleaseVersionVerified;
	}
	
	public boolean isLatestReleaseVersion() {
		return latestReleaseVersion;
	}
	
	public void setLatestReleaseVersion(boolean latestReleaseVersion) {
		this.latestReleaseVersion = latestReleaseVersion;
	}
}