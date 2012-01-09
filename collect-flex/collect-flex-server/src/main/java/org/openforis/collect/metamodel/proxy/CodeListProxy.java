/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.List;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeList.CodeScope;
import org.openforis.idm.metamodel.CodeList.CodeType;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLabel;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Survey;

/**
 * @author M. Togna
 *@author S. Ricci
 */
public class CodeListProxy implements ProxyBase {

	private transient CodeList codeList;

	public String getSinceVersionName() {
		return codeList.getSinceVersionName();
	}

	public String getDeprecatedVersionName() {
		return codeList.getDeprecatedVersionName();
	}

	public ModelVersion getSinceVersion() {
		return codeList.getSinceVersion();
	}

	public ModelVersion getDeprecatedVersion() {
		return codeList.getDeprecatedVersion();
	}

	public String getName() {
		return codeList.getName();
	}

	public List<CodeListLabel> getLabels() {
		return codeList.getLabels();
	}

	public List<LanguageSpecificText> getDescriptions() {
		return codeList.getDescriptions();
	}

	public List<CodeListLevel> getHierarchy() {
		return codeList.getHierarchy();
	}

	public List<CodeListItem> getItems() {
		return codeList.getItems();
	}

	public CodeType getCodeType() {
		return codeList.getCodeType();
	}

	public CodeScope getCodeScope() {
		return codeList.getCodeScope();
	}

	public boolean isAlphanumeric() {
		return codeList.isAlphanumeric();
	}

	public boolean isNumeric() {
		return codeList.isNumeric();
	}

	public Survey getSurvey() {
		return codeList.getSurvey();
	}
	
	
	
}
