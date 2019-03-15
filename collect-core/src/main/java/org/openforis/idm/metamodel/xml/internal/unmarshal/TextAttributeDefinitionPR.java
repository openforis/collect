package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.TEXT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.TYPE;

import java.io.IOException;
import java.util.Locale;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition.Type;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author G. Miceli
 */
class TextAttributeDefinitionPR extends AttributeDefinitionPR {


	public TextAttributeDefinitionPR() {
		super(TEXT);
	}

	@Override
	protected void onStartDefinition() throws XmlParseException, XmlPullParserException, IOException {
		super.onStartDefinition();
		String typeStr = getAttribute(TYPE, false); 
		TextAttributeDefinition defn = (TextAttributeDefinition) getDefinition();
		try {
			defn.setType(typeStr == null ? Type.SHORT : Type.valueOf(typeStr.toUpperCase(Locale.ENGLISH)));
		} catch (IllegalArgumentException e) {
			throw new XmlParseException(getParser(), "invalid type "+typeStr);
		}
	}

	@Override
	protected NodeDefinition createDefinition(int id) {
		Schema schema = getSchema();
		return schema.createTextAttributeDefinition(id);
	}
}