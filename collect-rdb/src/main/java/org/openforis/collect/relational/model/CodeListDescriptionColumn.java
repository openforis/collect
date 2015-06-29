/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.openforis.idm.metamodel.CodeListItem;

/**
 * @author S. Ricci
 *
 */
public class CodeListDescriptionColumn extends AbstractColumn<CodeListItem> {

	private static final int MAX_LENGTH = 511;
	
	private String languageCode;

	CodeListDescriptionColumn(String languageCode, String name) {
		super(name, Types.VARCHAR, "varchar", MAX_LENGTH, true);
		this.languageCode = languageCode;
	}

	public String getLanguageCode() {
		return languageCode;
	}

}
