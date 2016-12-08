package org.openforis.collect.designer.form;

import org.apache.commons.lang3.StringUtils;
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
	
	private String defaultLabel;
	private String defaultDescription;
	
	@Override
	public void loadFrom(CodeListItem source, String languageCode) {
		super.loadFrom(source, languageCode);
		code = source.getCode();
		label = source.getLabel(languageCode);
		description = source.getDescription(languageCode);
		qualifiable = source.isQualifiable();
		
		defaultLabel = source.getLabel();
		defaultDescription = source.getDescription();
	}
	
	@Override
	public void saveTo(CodeListItem dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setCode(StringUtils.trimToNull(code));
		dest.setLabel(languageCode, StringUtils.trimToNull(label));
		dest.setDescription(languageCode, StringUtils.trimToNull(description));
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
	
	public String getDefaultLabel() {
		return defaultLabel;
	}
	
	public String getDefaultDescription() {
		return defaultDescription;
	}

}
