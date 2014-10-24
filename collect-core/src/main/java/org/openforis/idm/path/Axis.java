package org.openforis.idm.path;

import java.util.List;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;

/**
 * 
 * @author G. Miceli
 *
 */
public interface Axis {
	List<Node<?>> evaluate(Record context);

	List<Node<?>> evaluate(Node<?> context);
	
	NodeDefinition evaluate(Schema context);
	
	NodeDefinition evaluate(NodeDefinition context);
}
