/**
 * 
 */
package org.openforis.collect.persistence.jooq;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.collect.persistence.jooq.tables.Data;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.File;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Node;

/**
 * @author M. Togna
 * 
 */
public class FileAttributeMapper extends NodeMapper {

	@Override
	Class<? extends NodeDefinition> getMappedClass() {
		return FileAttributeDefinition.class;
	}

	@Override
	void setFields(Node<?> node, InsertSetStep<?> insert) {
		FileAttribute f = (FileAttribute) node;
		File value = f.getValue();
		if (value != null) {
			insert.set(Data.DATA.TEXT1, value.getFilename());
			insert.set(Data.DATA.NUMBER1, toNumeric(value.getSize()));
		}

	}

	@Override
	Node<?> addNode(NodeDefinition defn, Record r, Entity parent) {
		String filename = r.getValueAsString(Data.DATA.TEXT1);
		Long size = r.getValueAsLong(Data.DATA.NUMBER1);
		File value = new File(filename, size);
		return parent.addValue(defn.getName(), value);
	}

}
