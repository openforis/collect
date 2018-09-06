package org.openforis.collect.relational.model;

import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.LanguageSpecificTextMap;


/**
 * 
 * @author S. Ricci
 *
 */
public class RelationalSchemaConfig implements Cloneable {

	private String idColumnPrefix;
	private String idColumnSuffix;
	private String pkConstraintPrefix;
	private String fkConstraintPrefix;
	private String codeListTableSuffix;
	private String codeListTablePrefix;
	private String labelColumnSuffix;
	private String descriptionColumnSuffix;
	private String dataTablePrefix;
	private String otherColumnSuffix;
	private int textMaxLength;
	private int memoMaxLength;
	private int floatingPointPrecision;
	private String defaultCode;
	private LanguageSpecificTextMap defaultCodeLabels;
	private boolean uniqueColumnNames;
	private boolean ancestorKeyColumnsIncluded;
	private boolean ancestorFKColumnsIncluded;
	
	private static final RelationalSchemaConfig DEFAULT;
	static {
		RelationalSchemaConfig config = new RelationalSchemaConfig();
		config.idColumnPrefix = "";
		config.idColumnSuffix = "_id_";
		config.pkConstraintPrefix = "pk_";
		config.fkConstraintPrefix = "fk_";
		config.codeListTableSuffix = "_code";
		config.codeListTablePrefix = "";
		config.labelColumnSuffix = "_label";
		config.descriptionColumnSuffix = "_desc";
		config.dataTablePrefix = "";
		config.otherColumnSuffix = "_other";
		config.textMaxLength = 255;
		config.memoMaxLength = 2048;
		config.floatingPointPrecision = 24;
		config.defaultCode = "-1";
		config.defaultCodeLabels = new LanguageSpecificTextMap();
		config.defaultCodeLabels.add(new LanguageSpecificText("en", "N/A"));
		config.uniqueColumnNames = true;
		config.ancestorKeyColumnsIncluded = false;
		config.ancestorFKColumnsIncluded = true;
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
	
	public String getDefaultCodeLabel(String langCode, String defaultLanguage) {
		if ( defaultCodeLabels == null ) {
			return null;
		} else {
			return defaultCodeLabels.getText(langCode, defaultLanguage, true);
		}
	}
	
	public String getIdColumnPrefix() {
		return idColumnPrefix;
	}
	
	public void setIdColumnPrefix(String idColumnPrefix) {
		this.idColumnPrefix = idColumnPrefix;
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

	public String getLabelColumnSuffix() {
		return labelColumnSuffix;
	}
	
	public void setLabelColumnSuffix(String labelColumnSuffix) {
		this.labelColumnSuffix = labelColumnSuffix;
	}
	
	public String getDescriptionColumnSuffix() {
		return descriptionColumnSuffix;
	}
	
	public void setDescriptionColumnSuffix(String descriptionColumnSuffix) {
		this.descriptionColumnSuffix = descriptionColumnSuffix;
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

	public boolean isUniqueColumnNames() {
		return uniqueColumnNames;
	}

	public void setUniqueColumnNames(boolean uniqueColumnNames) {
		this.uniqueColumnNames = uniqueColumnNames;
	}

	public boolean isAncestorKeyColumnsIncluded() {
		return ancestorKeyColumnsIncluded;
	}
	
	public void setAncestorKeyColumnsIncluded(boolean ancestorKeyColumnsIncluded) {
		this.ancestorKeyColumnsIncluded = ancestorKeyColumnsIncluded;
	}

	public boolean isAncestorFKColumnsIncluded() {
		return ancestorFKColumnsIncluded;
	}
	
	public void setAncestorFKColumnsIncluded(
			boolean ancestorFKColumnsIncluded) {
		this.ancestorFKColumnsIncluded = ancestorFKColumnsIncluded;
	}
}
