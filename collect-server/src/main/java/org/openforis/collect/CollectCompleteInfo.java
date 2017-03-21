package org.openforis.collect;

import org.openforis.commons.versioning.Version;

public class CollectCompleteInfo extends CollectInfo {

	private boolean latestReleaseVersionVerified;
	private boolean latestReleaseVersion;
	
	public CollectCompleteInfo(Version version, Version latestReleaseVersion) {
		this(version, latestReleaseVersion != null, 
				latestReleaseVersion == null ? false : version.compareTo(latestReleaseVersion) >= 0);
	}

	public CollectCompleteInfo(Version version, boolean latestVersionVerified, boolean latestVersion) {
		super(version);
		this.latestReleaseVersionVerified = latestVersionVerified;
		this.latestReleaseVersion = latestVersion;
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
