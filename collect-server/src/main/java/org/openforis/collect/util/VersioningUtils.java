/**
 * 
 */
package org.openforis.collect.util;

import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.VersionableModelDefinition;

/**
 * @author M. Togna
 * 
 */
public class VersioningUtils {

	public static boolean isValidVersion(ModelVersion modelVersion, ModelVersion since, ModelVersion deprecated) {
		if (modelVersion != null && since != null && deprecated != null) {
			return modelVersion.getPosition() >= since.getPosition() && modelVersion.getPosition() < deprecated.getPosition();
		} else if (since != null) {
			return modelVersion.getPosition() >= since.getPosition();
		} else if (deprecated != null) {
			return modelVersion.getPosition() < deprecated.getPosition();
		} else {
			return true;
		}
	}

	public static boolean hasValidVersion(VersionableModelDefinition versionableModelDefinition, ModelVersion modelVersion) {
		ModelVersion since = versionableModelDefinition.getSince();
		ModelVersion deprecated = versionableModelDefinition.getDeprecated();
		return isValidVersion(modelVersion, since, deprecated);
	}

}
