/**
 * 
 */
package org.openforis.collect.persistence.jooq.tables.records;

import org.jooq.impl.TableImpl;
import org.jooq.impl.UpdatableRecordImpl;

/**
 * @author M. Togna
 * 
 */
public class LookupRecord extends UpdatableRecordImpl<LookupRecord> {

	private static final long serialVersionUID = 1L;

	public LookupRecord(TableImpl<LookupRecord> table) {
		super(table);
	}
	
}
