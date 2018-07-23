package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.TEXT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.TYPE;

import java.io.IOException;
import java.util.Locale;

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
		attribute(TYPE, defn.getType().name().toLowerCase(Locale.ENGLISH));
	}
}
