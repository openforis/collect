/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.VersionableSurveyObject;

/**
 * @author S. Ricci
 *
 */
public class VersionableItemFormObject<T extends VersionableSurveyObject> extends SurveyObjectFormObject<T> {

	private int sinceVersionId;
	private int deprecatedVersionId;
	
	@Override
	public void loadFrom(T source, String languageCode) {
		super.loadFrom(source, languageCode);
		ModelVersion sinceVersion = source.getSinceVersion();
		sinceVersionId = sinceVersion != null ? sinceVersion.getId(): -1;
		ModelVersion deprecatedVersion = source.getDeprecatedVersion();
		deprecatedVersionId = deprecatedVersion != null ? deprecatedVersion.getId(): -1;
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		Survey survey = dest.getSurvey();
		ModelVersion sinceVersion = null;
		if ( sinceVersionId > 0 ) {
			sinceVersion = survey.getVersionById(sinceVersionId);
		}
		dest.setSinceVersion(sinceVersion);
		ModelVersion deprecatedVersion = null;
		if ( deprecatedVersionId > 0 ) {
			deprecatedVersion = survey.getVersionById(deprecatedVersionId);
		}
		dest.setDeprecatedVersion(deprecatedVersion);
	}

	@Override
	protected void reset() {
		sinceVersionId = -1;
		deprecatedVersionId = -1;
	}

	public int getSinceVersionId() {
		return sinceVersionId;
	}

	public void setSinceVersionId(int sinceVersionId) {
		this.sinceVersionId = sinceVersionId;
	}

	public int getDeprecatedVersionId() {
		return deprecatedVersionId;
	}

	public void setDeprecatedVersionId(int deprecatedVersionId) {
		this.deprecatedVersionId = deprecatedVersionId;
	}
	
}
