package org.openforis.collect.relational.model;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.LanguageSpecificTextMap;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus
 *
 */
public class CodeTable extends AbstractTable<CodeListItem> {
	
	private CodeList codeList;
	private CodeTable parent;
	private String defaultCode;
	private LanguageSpecificTextMap defaultCodeLabels;

	CodeTable(String prefix, String baseName, CodeList codeList, CodeTable parent, String defaultCode, LanguageSpecificTextMap defaultCodeLabels) {
		super(prefix, baseName);
		this.codeList = codeList;
		this.parent = parent;
		this.defaultCode = defaultCode;
		this.defaultCodeLabels = defaultCodeLabels;
	}
	
	public CodeList getCodeList() {
		return codeList;
	}

	public CodeTable getParent() {
		return parent;
	}
	
	public String getDefaultCode() {
		return defaultCode;
	}
	
	public LanguageSpecificTextMap getDefaultCodeLabels() {
		return defaultCodeLabels;
	}
	
	public String getDefaultCodeLabel(String langCode, String defaultLanguage) {
		if ( defaultCodeLabels == null ) {
			return null;
		} else {
			return defaultCodeLabels.getText(langCode, defaultLanguage);
		}
	}
	
	public Integer getLevelIdx() {
		Integer result = null;
		if ( codeList.isHierarchical() ) {
			result = 0;
			CodeTable cp = parent;
			while ( cp != null ) {
				result++;
				cp = cp.parent;
			}
		}
		return result;
	}

	public CodeListCodeColumn getCodeColumn() {
		for (Column<?> column : getColumns()) {
			if (column instanceof CodeListCodeColumn) {
				return (CodeListCodeColumn) column;
			}
		}
		return null;
	}
}
