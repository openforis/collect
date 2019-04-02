package org.openforis.idm.metamodel.xml.internal.unmarshal;

import java.io.IOException;
import java.util.Locale;

import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParserException;
import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

/**
 * @author G. Miceli
 */
abstract class NumericAttributeDefinitionPR extends AttributeDefinitionPR {

	private static final Type DEFAULT_TYPE = Type.REAL;

	public NumericAttributeDefinitionPR(String tagName) {
		super(tagName);
	}

	@Override
	protected void onStartDefinition() throws XmlParseException, XmlPullParserException, IOException {
		super.onStartDefinition();
		String typeStr = getAttribute(TYPE, false);
		NumericAttributeDefinition defn = (NumericAttributeDefinition) getDefinition();
		try {
			Type type = typeStr == null ? DEFAULT_TYPE : Type.valueOf(typeStr.toUpperCase(Locale.ENGLISH));
			defn.setType(type);
		} catch (IllegalArgumentException e) {
			throw new XmlParseException(getParser(), "unknown type " + typeStr);
		}
	}

}