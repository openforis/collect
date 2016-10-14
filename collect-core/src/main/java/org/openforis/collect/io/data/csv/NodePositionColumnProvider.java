package org.openforis.collect.io.data.csv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openforis.idm.model.Node;

/**
 * @author S. Ricci
 */
public class NodePositionColumnProvider implements ColumnProvider {
	
	private String headerName;

	public NodePositionColumnProvider(String headerName) {
		this.headerName = headerName;
	}
	
	public List<String> getColumnHeadings() {
		return Collections.unmodifiableList(Arrays.asList(headerName));
	}

	public List<String> extractValues(Node<?> axis) {
		if ( axis == null ) {
			throw new NullPointerException("Axis must be non-null");
		} else {
			int position = axis.getIndex() + 1;
			return Arrays.asList(Integer.toString(position));
		}
	}

}
