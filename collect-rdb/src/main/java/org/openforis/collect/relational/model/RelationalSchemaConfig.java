package org.openforis.collect.relational.model;

import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.LanguageSpecificTextMap;


/**
 * 
 * @author S. Ricci
 *
 */
public class RelationalSchemaConfig implements Cloneable {

	private String idColumnSuffix;
	private String pkConstraintPrefix;
	private String fkConstraintPrefix;
	private String codeListTableSuffix;
	private String codeListTablePrefix;
	private String labelColumnMiddleSuffix;
	private String descriptionColumnMiddleSuffix;
	private String dataTablePrefix;
	private String otherColumnSuffix;
	private int textMaxLength;
	private int memoMaxLength;
	private int floatingPointPrecision;
	private String defaultCode;
	private LanguageSpecificTextMap defaultCodeLabels;
	
	private static final RelationalSchemaConfig DEFAULT;
	static {
		RelationalSchemaConfig config = new RelationalSchemaConfig();
		config.idColumnSuffix = "_id";
		config.pkConstraintPrefix = "pk_";
		config.fkConstraintPrefix = "fk_";
		config.codeListTableSuffix = "_code";
		config.codeListTablePrefix = "";
		config.labelColumnMiddleSuffix = "_label_";
		config.descriptionColumnMiddleSuffix = "_desc_";
		config.dataTablePrefix = "";
		config.otherColumnSuffix = "_other";
		config.textMaxLength = 255;
		config.memoMaxLength = 2048;
		config.floatingPointPrecision = 24;
		config.defaultCode = "NA";
		config.defaultCodeLabels = new LanguageSpecificTextMap();
		config.defaultCodeLabels.add(new LanguageSpecificText("en", "N/A"));
		DEFAULT = config;
	}
	
	public static RelationalSchemaConfig createDefault() {
		try {
			return (RelationalSchemaConfig) DEFAULT.clone();
		} catch (CloneNotSupportedException e) {
			//it should never happen
			return null;
		}
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	public String getDefaultCodeLabel(String langCode) {
		if ( defaultCodeLabels == null ) {
			return null;
		} else {
			return defaultCodeLabels.getText(langCode);
		}
	}
	
	public String getIdColumnSuffix() {
		return idColumnSuffix;
	}

	public void setIdColumnSuffix(String idColumnSuffix) {
		this.idColumnSuffix = idColumnSuffix;
	}

	public String getPkConstraintPrefix() {
		return pkConstraintPrefix;
	}

	public void setPkConstraintPrefix(String pkConstraintPrefix) {
		this.pkConstraintPrefix = pkConstraintPrefix;
	}

	public String getFkConstraintPrefix() {
		return fkConstraintPrefix;
	}

	public void setFkConstraintPrefix(String fkConstraintPrefix) {
		this.fkConstraintPrefix = fkConstraintPrefix;
	}

	public String getCodeListTableSuffix() {
		return codeListTableSuffix;
	}

	public void setCodeListTableSuffix(String codeListTableSuffix) {
		this.codeListTableSuffix = codeListTableSuffix;
	}

	public String getCodeListTablePrefix() {
		return codeListTablePrefix;
	}

	public void setCodeListTablePrefix(String codeListTablePrefix) {
		this.codeListTablePrefix = codeListTablePrefix;
	}

	public String getLabelColumnMiddleSuffix() {
		return labelColumnMiddleSuffix;
	}

	public void setLabelColumnMiddleSuffix(String labelColumnMiddleSuffix) {
		this.labelColumnMiddleSuffix = labelColumnMiddleSuffix;
	}

	public String getDescriptionColumnMiddleSuffix() {
		return descriptionColumnMiddleSuffix;
	}

	public void setDescriptionColumnMiddleSuffix(
			String descriptionColumnMiddleSuffix) {
		this.descriptionColumnMiddleSuffix = descriptionColumnMiddleSuffix;
	}

	public String getDataTablePrefix() {
		return dataTablePrefix;
	}

	public void setDataTablePrefix(String dataTablePrefix) {
		this.dataTablePrefix = dataTablePrefix;
	}

	public String getOtherColumnSuffix() {
		return otherColumnSuffix;
	}

	public void setOtherColumnSuffix(String otherColumnSuffix) {
		this.otherColumnSuffix = otherColumnSuffix;
	}

	public int getTextMaxLength() {
		return textMaxLength;
	}

	public void setTextMaxLength(int textMaxLength) {
		this.textMaxLength = textMaxLength;
	}

	public int getMemoMaxLength() {
		return memoMaxLength;
	}

	public void setMemoMaxLength(int memoMaxLength) {
		this.memoMaxLength = memoMaxLength;
	}

	public int getFloatingPointPrecision() {
		return floatingPointPrecision;
	}

	public void setFloatingPointPrecision(int floatingPointPrecision) {
		this.floatingPointPrecision = floatingPointPrecision;
	}
	
	public String getDefaultCode() {
		return defaultCode;
	}

	public void setDefaultCode(String defaultCode) {
		this.defaultCode = defaultCode;
	}

	public LanguageSpecificTextMap getDefaultCodeLabels() {
		return defaultCodeLabels;
	}

	public void setDefaultCodeLabels(LanguageSpecificTextMap defaultCodeLabels) {
		this.defaultCodeLabels = defaultCodeLabels;
	}

}
