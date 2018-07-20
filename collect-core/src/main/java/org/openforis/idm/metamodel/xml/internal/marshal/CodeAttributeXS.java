package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.ALLOW_VALUES_SORTING;
import static org.openforis.idm.metamodel.xml.IdmlConstants.CODE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LIST;
import static org.openforis.idm.metamodel.xml.IdmlConstants.PARENT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.STRICT;

import java.io.IOException;

import org.openforis.idm.metamodel.CodeAttributeDefinition;

/**
 * 
 * @author G. Miceli
 *
 */
class CodeAttributeXS extends AttributeDefinitionXS<CodeAttributeDefinition> {

	CodeAttributeXS() {
		super(CODE);
	}
	
	@Override
	protected void attributes(CodeAttributeDefinition defn) throws IOException {
		super.attributes(defn);
		if ( defn.isAllowUnlisted() ) {
			attribute(STRICT, false);
		}
		attribute(LIST, defn.getListName());
		attribute(PARENT, defn.getParentExpression());
		if ( defn.isAllowValuesSorting() ) {
			attribute(ALLOW_VALUES_SORTING, true);
		}
	}
}
