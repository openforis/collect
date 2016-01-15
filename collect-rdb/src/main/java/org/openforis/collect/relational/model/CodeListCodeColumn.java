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
public class CodeListCodeColumn extends AbstractColumn<CodeListItem> {

	private static final int MAX_LENGTH = 255;

	CodeListCodeColumn(String name) {
		super(name,RDBJdbcType.VARCHAR, MAX_LENGTH, false);
	}

}
