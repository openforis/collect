package org.openforis.idm.metamodel.xml.internal.unmarshal;


import java.io.IOException;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParserException;
import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

/**
 * @author G. Miceli
 */
class NumberAttributeDefinitionPR extends NumericAttributeDefinitionPR {

	public NumberAttributeDefinitionPR() {
		super(NUMBER);
	}

	@Override
	protected void onStartDefinition() throws XmlParseException, XmlPullParserException, IOException {
		super.onStartDefinition();
		Boolean key = getBooleanAttribute(KEY, false);
		NumberAttributeDefinition defn = (NumberAttributeDefinition) getDefinition();
		defn.setKey(key == null ? false : key);
	}

	@Override
	protected NodeDefinition createDefinition(int id) {
		Schema schema = getSchema();
		return schema.createNumberAttributeDefinition(id);
	}
}