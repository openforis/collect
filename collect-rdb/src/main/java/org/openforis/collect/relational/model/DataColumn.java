package org.openforis.collect.relational.model;

import java.util.List;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.TimeAttribute;
import org.openforis.idm.path.Path;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataColumn extends AbstractColumn<Node<?>> {
	
	private NodeDefinition nodeDefinition;
	private Path relativePath;
	
	DataColumn(String name, int type, String typeName, NodeDefinition defn, Path relPath, Integer length, boolean nullable) {
		super(name, type, typeName, length, nullable);
		this.nodeDefinition = defn;
		this.relativePath = relPath;
	}

	public NodeDefinition getNodeDefinition() {
		return nodeDefinition;
	}
	
	public Path getRelativePath() {
		return relativePath;
	}

	@Override
	public Object extractValue(Node<?> context) {
		List<Node<?>> vals = relativePath.evaluate(context);
		if ( vals.size() > 1 ) {
			throw new RuntimeException("Path "+relativePath+" returned more than one value");
		}
		if ( vals.isEmpty() ) {
			return null;
		} else {
			Node<?> valNode = vals.get(0);
			Object val = convert(valNode);
			return val;
		}
	}

	private Object convert(Node<?> valNode) {
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
	}
	
}
