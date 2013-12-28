/**
 * 
 */
package org.openforis.collect.manager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openforis.collect.Collect;
import org.openforis.collect.model.ApplicationInfo;
import org.openforis.collect.persistence.ApplicationInfoDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class DatabaseVersionManager {

	private static final String VOID_VERSION = "PROJECT_VERSION"; //token was not being replaced into version.properties in previous releases
	
	private static final String[] MIGRATION_VERSIONS = new String[]{
		"3.0-Alpha2"
	};
	
	@Autowired
	private ApplicationInfoDao applicationInfoDao;
	
	public void checkIsVersionCompatible() throws DatabaseVersionNotCompatibleException {
		ApplicationInfo info = applicationInfoDao.load();
		String schemaVersion = null;
		if ( info != null ) {
			schemaVersion = info.getVersion();
		}
		String appVersion = Collect.getVersion();
		if ( ! isVersionCompatible(appVersion, schemaVersion) ) {
			throw new DatabaseVersionNotCompatibleException("Database version (" + 
					(schemaVersion != null ? schemaVersion: "not specified") + 
					") is not compatible with Application version: " + appVersion);
		}
	}
	
	private boolean isVersionCompatible(String appVersion, String schemaVersion) {
		if ( ( schemaVersion == null && appVersion.equals(VOID_VERSION) ) || appVersion.equals(schemaVersion) ) {
			return true;
		} else {
			String lastMigrationVersion = MIGRATION_VERSIONS[MIGRATION_VERSIONS.length - 1];
			VersionInfo appVersionInfo = VersionInfo.parse(appVersion);
			VersionInfo lastMigrationVersionInfo = VersionInfo.parse(lastMigrationVersion);
			if ( appVersionInfo.compareTo(lastMigrationVersionInfo) >= 0) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Extracts informations from a version number containing major, minor, revision, alpha and beta numbers.
	 * 
	 * @author S. Ricci
	 */
	public static class VersionInfo implements Comparable<VersionInfo> {
		private int major;
		private Integer minor;
		private Integer rev;
		private String testType;
		private Integer testVersion;
		private boolean snapshot;
		
		public int getMajor() {
			return major;
		}
		
		public void setMajor(int major) {
			this.major = major;
		}
		
		public Integer getMinor() {
			return minor;
		}
		
		public void setMinor(Integer minor) {
			this.minor = minor;
		}
		
		public Integer getRevision() {
			return rev;
		}
		
		public void setRevision(Integer rev) {
			this.rev = rev;
		}
		
		public String getTestType() {
			return testType;
		}
		
		public void setTestType(String testType) {
			this.testType = testType;
		}
		
		public boolean isAlpha() {
			return "Alpha".equals(testType);
		}
		
		public boolean isBeta() {
			return "Beta".equals(testType);
		}
		
		public Integer getTestVersion() {
			return testVersion;
		}
		
		public void setTestVersion(Integer testVersion) {
			this.testVersion = testVersion;
		}
		
		public boolean isSnapshot() {
			return snapshot;
		}
		
		public void setSnapshot(boolean snapshot) {
			this.snapshot = snapshot;
		}

		public int getTestLevel() {
			if ( isAlpha() ) {
				return 1;
			} else if ( isBeta() ) {
				return 2;
			} else {
				return Integer.MAX_VALUE;
			}
		}
		
		@Override
		public int compareTo(VersionInfo o) {
			int result = Integer.compare(major, o.major);
			if ( result == 0 ) {
				result = compareIntegers(minor, o.minor);
				if ( result == 0 ) {
					result = compareIntegers(rev, o.rev);
				}
			}
			if ( result == 0 ) {
				//compare test version
				result = Integer.compare(getTestLevel(), o.getTestLevel());
				if ( result == 0 ) {
					result = compareIntegers(testVersion, o.getTestVersion());
				}
			}
			if ( result == 0 ) {
				result = Boolean.compare(snapshot, o.snapshot);
			}
			return result;
		}
		
		private static int compareIntegers(Integer n1, Integer n2) {
			if ( n1 == null && n2 == null ) {
				return 0;
			} else if ( n1 == null ) {
				return -1;
			} else if ( n2 == null ) {
				return 1;
			} else {
				return Integer.compare(n1.intValue(), n2.intValue());
			}
		}
		
		/**
		 * Parses a version number.
		 * E.g. 3.0, 3.0.2, 3.0-Alpha2, 3.0-Alpha3-SNAPSHOT
		 */
		public static VersionInfo parse(String ver) {
		    Matcher m = Pattern.compile("(\\d+)\\.(\\d+)(\\.(\\d+))?(-(Alpha|Beta)(\\d+)?)?(-SNAPSHOT)?")
		                       .matcher(ver);
		    if (!m.matches())
		        throw new IllegalArgumentException("Malformed version number");

		    VersionInfo result = new VersionInfo();
		    result.setMajor(Integer.parseInt(m.group(1)));
		    result.setMinor(Integer.parseInt(m.group(2)));
		    if ( m.group(3) != null ) {
		    	result.setRevision(Integer.parseInt(m.group(4)));
		    }
		    //Alpha or Beta
		    if ( m.group(5) != null ) {
			    result.setTestType(m.group(6));
		    	if ( m.group(7) != null ) {
		    		result.setTestVersion(Integer.parseInt(m.group(7)));
		    	}
		    }
		    if ( m.group(8) != null ) {
		    	result.setSnapshot(true);
		    }
		    return result;
		}
		
	}
	
}
