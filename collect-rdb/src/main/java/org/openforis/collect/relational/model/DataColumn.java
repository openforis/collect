package org.openforis.collect.relational.model;

import java.util.List;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Node;
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
		return vals.isEmpty() ? null : vals.get(0);
	}
	
}
