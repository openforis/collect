/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.VersionableSurveyObject;

/**
 * @author S. Ricci
 *
 */
public class VersionableSurveyObjectProxy extends IdentifiableSurveyObjectProxy {

	private transient VersionableSurveyObject versionableSurveyObject;
	
	protected VersionableSurveyObjectProxy(
			VersionableSurveyObject versionableSurveyObject) {
		super(versionableSurveyObject);
		this.versionableSurveyObject = versionableSurveyObject;
	}

	@ExternalizedProperty
	public String getSinceVersionName() {
		ModelVersion sinceVersion = versionableSurveyObject.getSinceVersion();
		return sinceVersion != null ? sinceVersion.getName(): null;
	}

	@ExternalizedProperty
	public String getDeprecatedVersionName() {
		ModelVersion deprecatedVersion = versionableSurveyObject.getDeprecatedVersion();
		return deprecatedVersion != null ? deprecatedVersion.getName(): null;
	}

	@ExternalizedProperty
	public ModelVersionProxy getSinceVersion() {
		if (versionableSurveyObject.getSinceVersion() != null) {
			return new ModelVersionProxy(versionableSurveyObject.getSinceVersion());
		} else
			return null;
	}

	@ExternalizedProperty
	public ModelVersionProxy getDeprecatedVersion() {
		if (versionableSurveyObject.getDeprecatedVersion() != null) {
			return new ModelVersionProxy(versionableSurveyObject.getDeprecatedVersion());
		} else
			return null;
	}

}
