/**
 * 
 */
package org.openforis.collect.util;

import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Versionable;

/**
 * @author M. Togna
 * 
 */
public class VersioningUtils {

	public static boolean isValidVersion(ModelVersion version, ModelVersion since, ModelVersion deprecated) {
		if (version != null && since != null && deprecated != null) {
			return version.getPosition() >= since.getPosition() && version.getPosition() < deprecated.getPosition();
		} else if (since != null) {
			return version.getPosition() >= since.getPosition();
		} else if (deprecated != null) {
			return version.getPosition() < deprecated.getPosition();
		} else {
			return true;
		}
	}

	public static boolean hasValidVersion(Versionable versionable, ModelVersion version) {
		ModelVersion since = versionable.getSince();
		ModelVersion deprecated = versionable.getDeprecated();
		return isValidVersion(version, since, deprecated);
	}

}
