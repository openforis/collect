/**
 * 
 */
package org.openforis.collect.relational.model;

import org.openforis.collect.relational.sql.RDBJdbcType;
import org.openforis.idm.metamodel.CodeListItem;

/**
 * @author S. Ricci
 *
 */
public class CodeLabelColumn extends AbstractColumn<CodeListItem> {

	private static final int MAX_LENGTH = 255;
	
	private String languageCode;

	CodeLabelColumn(String languageCode, String name) {
		super(name, RDBJdbcType.VARCHAR, MAX_LENGTH, true);
		this.languageCode = languageCode;
	}

	public String getLanguageCode() {
		return languageCode;
	}

}
