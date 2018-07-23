package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.SCHEMA;

import java.io.IOException;

import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;

/**
 * 
 * @author G. Miceli
 *
 */
class SchemaXS extends XmlSerializerSupport<Schema, Survey> {

	SchemaXS() {
		super(SCHEMA);
		addChildMarshallers(new EntityDefinitionXS());
	}
	
	@Override
	protected void marshalInstances(Survey survey) throws IOException {
		Schema schema = survey.getSchema();
		marshal(schema);
	}
}
