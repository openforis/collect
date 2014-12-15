package org.openforis.idm.model;

import org.openforis.idm.metamodel.NodeDefinition;

/**
 * @author G. Miceli
 */
public interface NodeVisitor {

	void visit(Node<? extends NodeDefinition> node, int idx);

}
