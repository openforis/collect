/**
 * 
 */
package org.openforis.collect.manager;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.Collect;
import org.openforis.collect.model.ApplicationInfo;
import org.openforis.collect.persistence.ApplicationInfoDao;
import org.openforis.commons.versioning.Version;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class DatabaseVersionManager {

	private static final String[] MIGRATION_REQUIRED_VERSIONS = new String[]{
		"3.0-Alpha2"
	};
	
	@Autowired
	private ApplicationInfoDao applicationInfoDao;
	
	public void checkIsVersionCompatible() throws DatabaseVersionNotCompatibleException {
		ApplicationInfo info = applicationInfoDao.load();
		Version schemaVersion = null;
		if ( info != null && StringUtils.isNotBlank(info.getVersion()) ) {
			schemaVersion = new Version(info.getVersion());
		}
		Version appVersion = Collect.getVersion();
		if ( ! isVersionCompatible(appVersion, schemaVersion) ) {
			throw new DatabaseVersionNotCompatibleException("Database version (" + 
					(schemaVersion != null ? schemaVersion: "not specified") + 
					") is not compatible with Application version: " + appVersion);
		}
	}
	
	private boolean isVersionCompatible(Version appVersion, Version schemaVersion) {
		if ( ( schemaVersion == null && appVersion == null ) || appVersion.equals(schemaVersion) ) {
			return true;
		} else {
			String lastMigrationVersion = MIGRATION_REQUIRED_VERSIONS[MIGRATION_REQUIRED_VERSIONS.length - 1];
			Version lastMigrationVersionInfo = new Version(lastMigrationVersion);
			if ( appVersion.compareTo(lastMigrationVersionInfo) >= 0) {
				return true;
			} else {
				return false;
			}
		}
	}
	
}
