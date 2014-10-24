/**
 * 
 */
package org.openforis.idm.metamodel.expression;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.idm.AbstractTest;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;

/**
 * @author M. Togna
 *
 */
public class MetaModelExpressionTest extends AbstractTest {
	
	@Test
	public void testParentExpression(){
		Schema schema = survey.getSchema();
		EntityDefinition clusterDefn = schema.getRootEntityDefinition("cluster");
		NodeDefinition plotDefn = clusterDefn.getChildDefinition("plot");
		
		SchemaPathExpression expression = new SchemaPathExpression("parent()");
		NodeDefinition resultDefn = expression.evaluate(plotDefn);
		
		Assert.assertEquals(clusterDefn, resultDefn);
	}
	
}
