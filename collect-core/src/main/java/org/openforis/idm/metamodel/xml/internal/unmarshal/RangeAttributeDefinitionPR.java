package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.RANGE;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;

/**
 * @author G. Miceli
 */
class RangeAttributeDefinitionPR extends NumericAttributeDefinitionPR {

	public RangeAttributeDefinitionPR() {
		super(RANGE);
	}

	@Override
	protected NodeDefinition createDefinition(int id) {
		Schema schema = getSchema();
		return schema.createRangeAttributeDefinition(id);
	}
}