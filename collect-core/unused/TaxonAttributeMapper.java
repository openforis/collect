/**
 * 
 */
package org.openforis.collect.persistence.jooq;
import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.TaxonAttribute;

/**
 * @author M. Togna
 *
 */
class TaxonAttributeMapper extends NodeMapper {

	@Override
	Class<? extends NodeDefinition> getMappedClass() {
		return TaxonAttributeDefinition.class;
	}

	@Override
	void setFields(Node<?> node, InsertSetStep<?> insert) {
		//taxon name id
		//TODO
	}

	@Override
	Node<?> addNode(NodeDefinition defn, Record r, Entity parent) {
		// TODO
		return new TaxonAttribute((TaxonAttributeDefinition) defn);
	}

}
