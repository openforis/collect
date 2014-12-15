package org.openforis.collect.datacleansing;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Value;

/**
 * 
 * @author S. Ricci
 *
 */
public interface ValueParser {

	Value parseValue(AttributeDefinition def, String value);

}