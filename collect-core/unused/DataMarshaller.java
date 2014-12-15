package org.openforis.collect.persistence.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 */
public class DataMarshaller {

	public void write(CollectRecord record, Writer out) throws IOException {
		Entity rootEntity = record.getRootEntity();
		write(rootEntity, 0, out);
	}

	private void write(Node<?> node, int idx, Writer out) throws IOException {
		out.flush();
		if (node instanceof Entity) {
			writeEntity((Entity) node, out);
		} else if (node instanceof Attribute) {
			writeAttribute((Attribute<?,?>) node, out);
		}
	}

	private void writeAttribute(Attribute<?,?> attr, Writer out) throws IOException {
		out.write("\"");
		out.write(attr.getName());
		out.write("\":");
		int cnt = attr.getFieldCount();
		if (cnt == 1) {
			write(attr.getField(0), out);
		} else {
			out.write("[");
			for (int i = 0; i < cnt; i++) {
				if (i > 0) {
					out.write(",");
				}
				Field<?> fld = attr.getField(i);
				write(fld, out);
			}
			out.write("]");
		}
	}

	private void writeEntity(Entity entity, Writer out) throws IOException {
		out.write("{");
		out.write("\"");
		out.write(entity.getName());
		out.write("\":{");
		EntityDefinition defn = entity.getDefinition();
		List<NodeDefinition> childDefns = defn.getChildDefinitions();
		for (int j = 0; j < childDefns.size(); j++) {
			if ( j>0 ) {
				out.write(",");
			}
			NodeDefinition childDef = childDefns.get(j); 
			String name = childDef.getName();
			int childCount = entity.getCount(name);
			if ( childCount > 0 ) {
				if (childDef.isMultiple()) {
					out.write("[");
					for (int i = 0; i < childCount; i++) {
						if ( i>0 ) {
							out.write(",");
						}
						Node<?> child = entity.get(name, i);
						write(child, i, out);
					}
					out.write("]");
				} else {
					Node<?> child = entity.get(name, 0);
					write(child, 0, out);					
				}
			}
		}
		out.write("}}");
	}

	private void write(Field<?> field, Writer out) throws IOException {
		out.flush();
		Object val = field.getValue();
		if (val == null) {
			out.write("null");
		} else if (val instanceof String) {
			out.write("\"");
			val = ((String) val).replace("\\", "\\\\");
			val = ((String) val).replace("\"", "\\\"");
			val = ((String) val).replace("\n", "\\n");
			val = ((String) val).replace("\r", "\\r");
			out.write((String) val);
			out.write("\"");
		} else {
			out.write(val.toString());
		}
	}

}
