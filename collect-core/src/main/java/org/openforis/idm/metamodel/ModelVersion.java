/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class ModelVersion extends IdentifiableSurveyObject<ModelVersion> {

	private static final long serialVersionUID = 1L;

	private String name;
	private LanguageSpecificTextMap labels;
	private LanguageSpecificTextMap descriptions;
	private Date date;

	ModelVersion(Survey survey, int id) {
		super(survey, id);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<LanguageSpecificText> getLabels() {
		if ( this.labels == null ) {
			return Collections.emptyList();
		} else {
			return this.labels.values();
		}
	}
	
	public String getLabel(String language) {
		return getLabel(language, false);
	}
	
	public String getLabel(String language, boolean defaultToSurveyDefaultLanguage) {
		String defaultLanguage = defaultToSurveyDefaultLanguage ? getSurvey().getDefaultLanguage() : null;
		return labels == null ? null: labels.getText(language, defaultLanguage);
	}
	
	public void addLabel(LanguageSpecificText label) {
		if ( labels == null ) {
			labels = new LanguageSpecificTextMap();
		}
		labels.add(label);
	}

	public void setLabel(String language, String text) {
		if ( labels == null ) {
			labels = new LanguageSpecificTextMap();
		}
		labels.setText(language, text);
	}
	
	public void removeLabel(String language) {
		labels.remove(language);
	}

	public List<LanguageSpecificText> getDescriptions() {
		if ( this.descriptions == null ) {
			return Collections.emptyList();
		} else {
			return this.descriptions.values();
		}
	}

	public String getDescription(String language) {
		return getDescription(language, false);
	}
	
	public String getDescription(String language, boolean defaultToSurveyDefaultLanguage) {
		String defaultLanguage = defaultToSurveyDefaultLanguage ? getSurvey().getDefaultLanguage() : null;
		return descriptions == null ? null: descriptions.getText(language, defaultLanguage);
	}
	
	public void setDescription(String language, String description) {
		if ( descriptions == null ) {
			descriptions = new LanguageSpecificTextMap();
		}
		descriptions.setText(language, description);
	}
	
	public void addDescription(LanguageSpecificText description) {
		if ( descriptions == null ) {
			descriptions = new LanguageSpecificTextMap();
		}
		descriptions.add(description);
	}

	public void removeDescription(String language) {
		descriptions.remove(language);
	}

	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isApplicable(VersionableSurveyObject versionable) {
		ModelVersion since = versionable.getSinceVersion();
		ModelVersion deprecated = versionable.getDeprecatedVersion();
		if (since == null && deprecated == null) {
			return true;
		} else {
			int sinceResult = this.compareTo(since);
			
			if ( sinceResult == 0 && this.getId() != since.getId() ) {
				//versions have the same date but they are not the same, consider the versionable object not applicable to this version
				//OFC-1500 - Support model versions with same date
				return false;
			}
			
			int deprecatedResult;
			
			if ( deprecated == null ) {
				//if deprecated is not specified, always consider the version in the specified versionable \greater than the current one
				deprecatedResult = -1;
			} else {
				deprecatedResult = this.compareTo(deprecated);
			}
			return sinceResult >= 0 && deprecatedResult < 0;
		}
	}
	
	@Override
	public int compareTo(ModelVersion v) {
		if ( v == null ) {
			return 1;
		} else if ( this.getId() == v.getId() ) {
			return 0;
		} else {
			int result = date.compareTo(v.getDate());
			return result;
		}
	}
	
	public <T extends VersionableSurveyObject> List<T> filterApplicableItems(List<T> list) {
		List<T> result = new ArrayList<T>();
		for (T item : list) {
			if ( isApplicable(item) ) {
				result.add(item);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{name: ");
		sb.append(name);
		sb.append(", date: ");
		sb.append(date);
		sb.append("}");
		return sb.toString();
	}

	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelVersion other = (ModelVersion) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (descriptions == null) {
			if (other.descriptions != null)
				return false;
		} else if (!descriptions.equals(other.descriptions))
			return false;
		if (labels == null) {
			if (other.labels != null)
				return false;
		} else if (!labels.equals(other.labels))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
