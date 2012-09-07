/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Versionable;

/**
 * @author S. Ricci
 *
 */
public class VersionableItemFormObject<T extends Versionable> extends ItemFormObject<T> {

	private ModelVersion sinceVersion;
	
	private ModelVersion deprecatedVersion;
	
	public void loadFrom(T source, String languageCode) {
		sinceVersion = source.getSinceVersion();
		deprecatedVersion = source.getDeprecatedVersion();
	}
	
	public void saveTo(T dest, String languageCode) {
		dest.setSinceVersion(sinceVersion);
		dest.setDeprecatedVersion(deprecatedVersion);
	}

	public ModelVersion getSinceVersion() {
		return sinceVersion;
	}

	public void setSinceVersion(ModelVersion sinceVersion) {
		this.sinceVersion = sinceVersion;
	}

	public ModelVersion getDeprecatedVersion() {
		return deprecatedVersion;
	}

	public void setDeprecatedVersion(ModelVersion deprecatedVersion) {
		this.deprecatedVersion = deprecatedVersion;
	}
}
