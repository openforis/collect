package org.openforis.collect.model;

import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Versionable;

/**
 * 
 * @author S. Ricci
 *
 */
@Deprecated
public class ModelVersionUtil {
	
	public static boolean isInVersion(Versionable versionable, ModelVersion currentVersion) {
		boolean result;
		ModelVersion since = versionable.getSinceVersion();
		ModelVersion deprecated = versionable.getDeprecatedVersion();
		String currentDate = currentVersion.getDate();
		int compareToSince; 
		if(since == null) {
			compareToSince = 1;
		} else {
			compareToSince = currentDate.compareTo(since.getDate());
		}
		int compareToDeprecated;
		if(deprecated == null) {
			compareToDeprecated = -1;
		} else {
			compareToDeprecated = currentDate.compareTo(deprecated.getDate());
		}
		result = compareToSince >= 0 && compareToDeprecated <=0;
		return result;
	}

}
