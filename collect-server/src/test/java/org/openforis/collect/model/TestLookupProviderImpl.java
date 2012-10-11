/**
 * 
 */
package org.openforis.collect.model;

import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.collect.model.expression.LookupFunctionTest;

/**
 * @author M. Togna
 * 
 */
public class TestLookupProviderImpl implements LookupProvider {

	@Override
	public Object lookup(String name, String attribute, Object... keys) {
		return LookupFunctionTest.TEST_COORDINATE;
	}

}