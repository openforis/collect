package org.openforis.collect.relational;

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
	
	DataColumn(String name, int type, Integer length, boolean allowNulls, NodeDefinition defn, Path relativePath) {
		super(name, type, length, allowNulls);
		this.nodeDefinition = defn;
		this.relativePath = relativePath;
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
