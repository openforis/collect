/**
 * 
 */
package org.openforis.collect.model;

import org.openforis.collect.model.expression.LookupFunctionTest;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.LookupProvider;

/**
 * @author M. Togna
 * 
 */
public class TestLookupProviderImpl implements LookupProvider {

	@Override
	public Object lookup(Survey survey, String name, String attribute, Object... keys) {
		return LookupFunctionTest.TEST_COORDINATE.toString();
	}

}