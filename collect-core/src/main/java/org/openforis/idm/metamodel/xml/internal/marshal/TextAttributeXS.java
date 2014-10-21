package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

import java.io.IOException;

import org.openforis.idm.metamodel.TextAttributeDefinition;

/**
 * 
 * @author G. Miceli
 *
 */
class TextAttributeXS extends AttributeDefinitionXS<TextAttributeDefinition> {

	protected TextAttributeXS() {
		super(TEXT);
	}


	@Override
	protected void attributes(TextAttributeDefinition defn) throws IOException {
		super.attributes(defn);
		attribute(TYPE, defn.getType().name().toLowerCase());
		if ( defn.isKey() ) {
			attribute(KEY, true);
		}
	}
}
