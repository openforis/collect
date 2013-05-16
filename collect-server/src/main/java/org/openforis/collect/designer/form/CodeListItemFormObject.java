package org.openforis.collect.designer.form;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeListItem;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListItemFormObject extends VersionableItemFormObject<CodeListItem> {

	private String code;
	private String label;
	private String description;
	private boolean qualifiable;
	
	@Override
	public void loadFrom(CodeListItem source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		code = source.getCode();
		label = getLabel(source, languageCode, defaultLanguage);
		description = getDescription(source, languageCode, defaultLanguage);
		qualifiable = source.isQualifiable();
	}
	
	@Override
	public void saveTo(CodeListItem dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setCode(code);
		saveLabel(dest, languageCode);
		saveDescription(dest, languageCode);
		dest.setQualifiable(qualifiable);
	}

	protected String getLabel(CodeListItem source, String languageCode, String defaultLanguage) {
		String result = source.getLabel(languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getLabel(null);
		}
		return result;
	}
	
	protected String getDescription(CodeListItem source, String languageCode, String defaultLanguage) {
		String result = source.getDescription(languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getDescription(null);
		}
		return result;
	}

	protected void saveLabel(CodeListItem dest, String languageCode) {
		CollectSurvey survey = (CollectSurvey) dest.getSurvey();
		String defaultLanguage = survey.getDefaultLanguage();
		if ( defaultLanguage.equals(languageCode) ) {
			//remove default label
			dest.removeLabel(null);
		}
		dest.setLabel(languageCode, label);
	}
	
	protected void saveDescription(CodeListItem dest, String languageCode) {
		CollectSurvey survey = (CollectSurvey) dest.getSurvey();
		String defaultLanguage = survey.getDefaultLanguage();
		if ( defaultLanguage.equals(languageCode) ) {
			//remove default label
			dest.removeDescription(null);
		}
		dest.setDescription(languageCode, description);
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isQualifiable() {
		return qualifiable;
	}

	public void setQualifiable(boolean qualifiable) {
		this.qualifiable = qualifiable;
	}

}
