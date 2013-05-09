/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.openforis.collect.relational.DatabaseExporterConfig;
import org.openforis.idm.metamodel.CodeListItem;

/**
 * @author S. Ricci
 *
 */
public class CodeListCodeColumn extends AbstractColumn<CodeListItem> {

	private static final int MAX_LENGTH = 255;

	CodeListCodeColumn(String name) {
		super(name, Types.VARCHAR, "varchar", MAX_LENGTH, false);
	}

	@Override
	public Object extractValue(CodeListItem item) {
		return extractValue(DatabaseExporterConfig.createDefault(), item);
	}

	@Override
	public Object extractValue(DatabaseExporterConfig config,
			CodeListItem item) {
		return item.getCode();
	}

}
