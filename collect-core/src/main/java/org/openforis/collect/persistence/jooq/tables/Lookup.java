/**
 * 
 */
package org.openforis.collect.persistence.jooq.tables;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.jooq.DataType;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.openforis.collect.persistence.jooq.Collect;
import org.openforis.collect.persistence.jooq.tables.records.LookupRecord;
import org.openforis.collect.persistence.utils.TableMetaData;
import org.openforis.collect.persistence.utils.TableMetaData.ColumnMetaData;

/**
 * @author M. Togna
 * 
 */
public class Lookup extends TableImpl<LookupRecord> {

	private static final long serialVersionUID = 1L;
	private static Map<String, Lookup> nameToLookup;
	
	private boolean initialized = false;
	public final org.jooq.TableField<LookupRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this);

	public static Lookup getInstance(String name) {
		if (nameToLookup == null) {
			nameToLookup = new HashMap<String, Lookup>();
		}
		Lookup lookup = nameToLookup.get(name);
		if ( lookup == null ) {
			lookup = new Lookup(name);
			nameToLookup.put(name, lookup);
		}
		return lookup;
	}
	
	private Lookup(String name) {
		super(name, Collect.COLLECT);
	}
	
	public void initialize(TableMetaData tableMetaData) {
		for (ColumnMetaData colMetadata : tableMetaData.getColumnsMetaData()) {
			String colName = colMetadata.getName();
			if ( this.field(colName) == null ) {
				Integer dataType = colMetadata.getDataType();
				this.createField(colName, dataType);
			}
		}
		initialized = true;
	}

	public TableField<LookupRecord, String> createFieldByName(String name) {
		return createField(name, org.jooq.impl.SQLDataType.VARCHAR, this);
	}

	public TableField<LookupRecord, Integer> createIntegerField(String name) {
		return createField(name, org.jooq.impl.SQLDataType.INTEGER, this);
	}
	
	public TableField<LookupRecord, ?> createField(String name, int sqlDataType) {
		return createField(name, getDataType(sqlDataType), this);
	}

	private DataType<?> getDataType(int sqlDataType) {
		switch(sqlDataType) {
		case Types.INTEGER:
			return SQLDataType.INTEGER;
		case Types.CHAR:
			return SQLDataType.CHAR;
		case Types.VARCHAR:
			return SQLDataType.VARCHAR;
		default:
			throw new IllegalArgumentException("Unsupported SQL data type: " + sqlDataType);
		}
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
}
