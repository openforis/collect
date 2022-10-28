package org.openforis.collect.io.data.csv.columnProviders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.io.data.csv.Column;
import org.openforis.collect.io.data.csv.Column.DataType;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.User;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;

/**
 * @author S. Ricci
 */
public class CreatedByUserColumnProvider implements ColumnProvider {
	
	private static final String CREATED_BY_COLUMN_NAME = "_created_by";
	
	public List<Column> getColumns() {
		return Collections.unmodifiableList(Arrays.asList(new Column(CREATED_BY_COLUMN_NAME, DataType.STRING)));
	}

	public List<Object> extractValues(Node<?> axis) {
		if ( axis == null ) {
			throw new NullPointerException("Axis must be non-null");
		} else {
			Record record = axis.getRecord();
			if (record instanceof CollectRecord) {
				User user = ((CollectRecord) record).getCreatedBy();
				return user == null ? null : Collections.<Object>singletonList(user.getUsername());
			} else {
				return null;
			}
		}
	}

}
