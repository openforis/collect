package org.openforis.collect.persistence.jooq;

import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 */
abstract class NodeMapper {
	
	abstract Class<? extends NodeDefinition> getMappedClass(); 
	
	abstract void setInsertFields(Node<?> node, InsertSetStep<?> insert);
	
	abstract Node<?> addObject(NodeDefinition defn, Record r, Entity parent);
}
