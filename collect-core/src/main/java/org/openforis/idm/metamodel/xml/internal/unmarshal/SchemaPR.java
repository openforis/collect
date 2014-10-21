package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

/**
 * @author G. Miceli
 */
class SchemaPR extends IdmlPullReader {

	public SchemaPR() {
		super(SCHEMA);
		addChildPullReaders(new EntityDefinitionPR());
	}
}