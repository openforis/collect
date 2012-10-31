/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.VersionableSurveyObject;

/**
 * @author S. Ricci
 *
 */
public class VersionableItemFormObject<T extends VersionableSurveyObject> extends FormObject<T> {

	private ModelVersion sinceVersion;
	private ModelVersion deprecatedVersion;
	
	@Override
	public void loadFrom(T source, String languageCode, String defaultLanugage) {
		sinceVersion = source.getSinceVersion();
		deprecatedVersion = source.getDeprecatedVersion();
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		dest.setSinceVersion(sinceVersion);
		dest.setDeprecatedVersion(deprecatedVersion);
	}

	@Override
	protected void reset() {
		sinceVersion = null;
		deprecatedVersion = null;
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
