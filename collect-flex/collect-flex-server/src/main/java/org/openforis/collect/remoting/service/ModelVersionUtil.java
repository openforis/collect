package org.openforis.collect.remoting.service;

import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Versionable;

/**
 * 
 * @author S. Ricci
 *
 */
public class ModelVersionUtil {
	
	public static boolean isInVersion(Versionable versionable, ModelVersion currentVersion) {
		boolean result;
		ModelVersion since = versionable.getSinceVersion();
		ModelVersion deprecated = versionable.getDeprecatedVersion();
		String currentDate = currentVersion.getDate();
		int compareToSince = since != null ? currentDate.compareTo(since.getDate()): 1;
		int compareToDeprecated = deprecated != null ? currentDate.compareTo(deprecated.getDate()): -1;
		
		result = compareToSince >= 0 && compareToDeprecated <=0;
		return result;
	}

}
