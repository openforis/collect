/**
 * 
 */
package org.openforis.collect.manager;

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
			if ( appVersion.compareTo(lastMigrationVersion) >= 0) {
				return true;
			} else {
				return false;
			}
		}
	}
	
}
