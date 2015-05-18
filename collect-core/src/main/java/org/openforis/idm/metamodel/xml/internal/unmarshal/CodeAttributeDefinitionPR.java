package org.openforis.idm.metamodel.xml.internal.unmarshal;


import static org.openforis.idm.metamodel.xml.IdmlConstants.ALLOW_VALUES_SORTING;
import static org.openforis.idm.metamodel.xml.IdmlConstants.CODE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LIST;
import static org.openforis.idm.metamodel.xml.IdmlConstants.PARENT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.STRICT;

import java.io.IOException;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author G. Miceli
 */
class CodeAttributeDefinitionPR extends AttributeDefinitionPR {

	public CodeAttributeDefinitionPR() {
		super(CODE);
	}
	@Override
	protected void onStartDefinition() throws XmlParseException, XmlPullParserException, IOException {
		super.onStartDefinition();
		String parent = getAttribute(PARENT, false);
		Boolean strict = getBooleanAttribute(STRICT, false);
		String listName = getAttribute(LIST, true);
		Boolean allowValuesSorting = getBooleanAttribute(ALLOW_VALUES_SORTING, false);
		CodeAttributeDefinition defn = (CodeAttributeDefinition) getDefinition();
		defn.setParentExpression(parent);
		defn.setAllowUnlisted(strict == null ? false : ! strict);
		defn.setListName(listName);
		defn.setAllowValuesSorting(allowValuesSorting == null ? false: allowValuesSorting.booleanValue());
	}
	
	@Override
	protected NodeDefinition createDefinition(int id) {
		Schema schema = getSchema();
		return schema.createCodeAttributeDefinition(id);
	}
}