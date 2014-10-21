package org.openforis.idm.metamodel.xml.internal.unmarshal;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

/**
 * @author G. Miceli
 */
class CoordinateAttributeDefinitionPR extends AttributeDefinitionPR {

	public CoordinateAttributeDefinitionPR() {
		super(COORDINATE);
	}

	@Override
	protected NodeDefinition createDefinition(int id) {
		Schema schema = getSchema();
		return schema.createCoordinateAttributeDefinition(id);
	}
}