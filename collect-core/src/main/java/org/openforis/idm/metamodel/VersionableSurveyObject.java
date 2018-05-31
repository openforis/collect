package org.openforis.idm.metamodel;


/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public abstract class VersionableSurveyObject extends IdentifiableSurveyObject<VersionableSurveyObject> {

	private static final long serialVersionUID = 1L;

	private ModelVersion sinceVersion;
	private ModelVersion deprecatedVersion;

	protected VersionableSurveyObject(Survey survey, int id) {
		super(survey, id);
	}

	protected VersionableSurveyObject(Survey survey, VersionableSurveyObject object, int id) {
		super(survey, object, id);
		if (survey == object.getSurvey()) {
			this.sinceVersion = object.sinceVersion;
			this.deprecatedVersion = object.deprecatedVersion;
		}
	}
	
	public void removeVersioning(ModelVersion version) {
		int versionId = version.getId();
		if ( sinceVersion != null && sinceVersion.getId() == versionId ) {
			sinceVersion = null;
		}
		if ( deprecatedVersion != null && deprecatedVersion.getId() == versionId ) {
			deprecatedVersion = null;
		}
	}

	public String getSinceVersionName() {
		return sinceVersion == null ? null : sinceVersion.getName();
	}
	
	public void setSinceVersionByName(String name) {
		this.sinceVersion = name == null ? null : findVersion(name);
	}

	public String getDeprecatedVersionName() {
		return deprecatedVersion == null ? null : deprecatedVersion.getName();
	}
	
	public void setDeprecatedVersionByName(String name) {
		this.deprecatedVersion = name == null ? null : findVersion(name);
	}

	public ModelVersion getSinceVersion() {
		return this.sinceVersion;
	}

	public void setSinceVersion(ModelVersion since) {
		this.sinceVersion = since;
	}

	public ModelVersion getDeprecatedVersion() {
		return this.deprecatedVersion;
	}

	public void setDeprecatedVersion(ModelVersion deprecated) {
		this.deprecatedVersion = deprecated;
	}

	private ModelVersion findVersion(String name) {
		if ( name == null ) {
			return null;
		} else {
			Survey survey = getSurvey();
			if ( survey == null ) {
				throw new IllegalStateException("Survey not set!");
			} 
			ModelVersion v = survey.getVersion(name);
			if ( v == null ) {
				throw new IllegalArgumentException("Undefined version '"+name+"' in "+toString());
			} 
			return v;
		}
	}

	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VersionableSurveyObject other = (VersionableSurveyObject) obj;
		if (deprecatedVersion == null) {
			if (other.deprecatedVersion != null)
				return false;
		} else if (!deprecatedVersion.deepEquals(other.deprecatedVersion))
			return false;
		if (sinceVersion == null) {
			if (other.sinceVersion != null)
				return false;
		} else if (!sinceVersion.deepEquals(other.sinceVersion))
			return false;
		return true;
	}
}
