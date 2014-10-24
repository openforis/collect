package org.openforis.idm.metamodel.xml.internal.unmarshal;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

/**
 * @author G. Miceli
 */
class DateAttributeDefinitionPR extends AttributeDefinitionPR {

	public DateAttributeDefinitionPR() {
		super(DATE);
	}
	@Override
	protected NodeDefinition createDefinition(int id) {
		Schema schema = getSchema();
		return schema.createDateAttributeDefinition(id);
	}
}