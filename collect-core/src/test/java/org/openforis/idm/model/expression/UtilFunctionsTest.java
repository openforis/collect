/**
 * 
 */
package org.openforis.idm.model.expression;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * @author S. Ricci
 *
 */
public class UtilFunctionsTest extends AbstractExpressionTest {

	@Test
	public void testUuid() throws InvalidExpressionException{
		String uuid = (String) evaluateExpression(cluster, "util:uuid()");
		assertNotNull(uuid);
	}
	
}
