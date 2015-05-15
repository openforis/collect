package org.openforis.collect.relational.util;

import javax.xml.namespace.QName;

/**
 * 
 * @author S. Ricci
 *
 */
public interface Constants {
	
	public static final String RDB_NAMESPACE = "http://www.openforis.org/collect/3.0/rdb";
	public static final QName TABLE_NAME_QNAME = new QName(RDB_NAMESPACE, "table");
	public static final QName COLUMN_NAME_QNAME = new QName(RDB_NAMESPACE, "column");
	public static final String DATA_TABLE_PK_FORMAT = "%s%s%s";
	public static final String CODE_TABLE_PK_FORMAT = "%s%s";
	
}
