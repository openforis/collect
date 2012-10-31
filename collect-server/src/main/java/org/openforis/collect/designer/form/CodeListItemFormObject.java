package org.openforis.collect.designer.form;

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
		label = source.getLabel(languageCode);
		description = source.getDescription(languageCode);
		qualifiable = source.isQualifiable();
	}
	
	@Override
	public void saveTo(CodeListItem dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setCode(code);
		dest.setLabel(languageCode, label);
		dest.setDescription(languageCode, description);
		dest.setQualifiable(qualifiable);
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
