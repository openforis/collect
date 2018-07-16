package org.openforis.collect.io.data.csv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.io.data.csv.Column.DataType;
import org.openforis.idm.model.Node;

/**
 * @author S. Ricci
 */
public class NodePositionColumnProvider implements ColumnProvider {
	
	private String headerName;

	public NodePositionColumnProvider(String headerName) {
		this.headerName = headerName;
	}
	
	public List<Column> getColumns() {
		return Collections.unmodifiableList(Arrays.asList(new Column(headerName, DataType.INTEGER)));
	}

	public List<Object> extractValues(Node<?> axis) {
		if ( axis == null ) {
			throw new NullPointerException("Axis must be non-null");
		} else {
			int position = axis.getIndex() + 1;
			return Arrays.<Object>asList(position);
		}
	}

}
