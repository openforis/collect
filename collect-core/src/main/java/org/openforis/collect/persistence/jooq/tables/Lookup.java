/**
 * 
 */
package org.openforis.collect.persistence.jooq.tables;

import org.jooq.TableField;
import org.jooq.impl.UpdatableTableImpl;
import org.openforis.collect.persistence.jooq.Collect;
import org.openforis.collect.persistence.jooq.tables.records.LookupRecord;

/**
 * @author M. Togna
 * 
 */
public class Lookup extends UpdatableTableImpl<LookupRecord> {

	private static final long serialVersionUID = 1L;
	private static Lookup lookup;

	public static Lookup getInstance(String name) {
		if (lookup == null) {
			lookup = new Lookup(name);
		}
		return lookup;
	}

	// public Lookup(String name, Schema schema, Table<LookupRecord> aliased) {
	// super(name, schema, aliased);
	// }
	//
	// public Lookup(String name, Schema schema) {
	// super(name, schema);
	// }

	private Lookup(String name) {
		super(name, Collect.COLLECT);
	}

	public final org.jooq.TableField<LookupRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this);

	public TableField<LookupRecord, String> getFieldByName(String name) {
		return createField(name, org.jooq.impl.SQLDataType.VARCHAR, this);
	}

}
