package org.openforis.collect.relational.data.internal;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.sql.RDBJdbcType;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.TimeAttribute;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataTableDataColumnValueExtractor<C extends DataColumn> extends ColumnValueExtractor<DataTable, C> {

	private static final Logger LOG = LogManager.getLogger(DataTableDataColumnValueExtractor.class);
	
	public DataTableDataColumnValueExtractor(DataTable table, C column) {
		super(table, column);
	}
	
	@Override
	public Object extractValue(Node<?> context) {
		Node<?> valNode = extractValueNode(context);
		Object val = extractNodeValue(valNode);
		if ( RDBJdbcType.VARCHAR == column.getType() && val != null) {
			Integer colLength = column.getLength();
			int valLength = val.toString().length();
			if (valLength > colLength) {
				LOG.warn(String.format("Record: %d. Value of node %s (%s) has a length of %d characters"
						+ " and exceeds the maximum allowed (%d), so it has been truncated", 
						context.getRecord().getId(), valNode.getPath(), val, valLength, colLength));
				val = ((String) val).substring(0, colLength);
			}
		}
		if (isNullOrNaN(val)) {
			return column.getDefaultValue();
		} else {
			return val;
		}
	}

	protected Node<?> extractValueNode(Node<?> context) {
		List<Node<?>> vals = column.getRelativePath().evaluate(context);
		if ( vals.size() > 1 ) {
			LOG.warn(String.format("Record: %s - path %s returned more than one value", 
					((CollectRecord) context.getRecord()).getRootEntityKeyValues(), column.getRelativePath()));
		}
		if ( vals.isEmpty() ) {
			return null;
		} else {
			return vals.get(0);
		}
	}
	
	private Object extractNodeValue(Node<?> valNode) {
		if ( valNode == null ) {
			return null;
		}
		try {
			if ( valNode instanceof Field ) {
				return ((Field<?>) valNode).getValue();
			} else if ( valNode instanceof DateAttribute ) {
				Date date = ((DateAttribute) valNode).getValue();
				return date.toJavaDate();
			} else if ( valNode instanceof TimeAttribute ) {
				Time time = ((TimeAttribute) valNode).getValue();
				return time.toXmlTime();
			} else if ( valNode instanceof Attribute ) {
				return ((Attribute<?,?>) valNode).getValue();
			} else {
				throw new RuntimeException("Unknown data node type "+valNode.getClass());
			}
		} catch ( Exception e) {
			//ERRORS in data?
			String messageFormat = "Error converting attribute value in record: %d - node: %s";
			String message = String.format(messageFormat, valNode.getRecord().getId(), valNode.getPath());
			LOG.error(message);
			System.out.println(message);
			return null;
		}
	}
	
	private static boolean isNullOrNaN(Object val) {
		return val == null 
				|| val instanceof Double && Double.isNaN((Double) val) 
				|| val instanceof Float && Float.isNaN((Float) val);
	}
	
}
