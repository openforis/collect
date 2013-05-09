package org.openforis.collect.relational;

import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.LanguageSpecificTextMap;

/**
 * 
 * @author S. Ricci
 *
 */
public class DatabaseExporterConfig implements Cloneable {
	
	private String defaultCode;
	private LanguageSpecificTextMap defaultCodeLabels;
	
	private static final DatabaseExporterConfig DEFAULT;
	static {
		DatabaseExporterConfig config = new DatabaseExporterConfig();
		config.defaultCode = "NA";
		config.defaultCodeLabels = new LanguageSpecificTextMap();
		config.defaultCodeLabels.add(new LanguageSpecificText("en", "N/A"));
		DEFAULT = config;
	}
	
	public static DatabaseExporterConfig createDefault() {
		try {
			return (DatabaseExporterConfig) DEFAULT.clone();
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
		return defaultCodeLabels.getText(langCode);
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
