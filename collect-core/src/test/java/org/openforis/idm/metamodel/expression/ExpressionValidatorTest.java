/**
 * 
 */
package org.openforis.idm.metamodel.expression;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openforis.idm.AbstractTest;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;

/**
 * @author S. Ricci
 *
 */
public class ExpressionValidatorTest extends AbstractTest {
	
	private ExpressionValidator validator;

	@Before
	public void init() {
		validator = new ExpressionValidator(survey.getContext().getExpressionFactory());
	}
	
	@Test
	@Ignore
	public void testCircularDependencies() {
		Schema schema = survey.getSchema();
		EntityDefinition clusterDefn = schema.getRootEntityDefinition("cluster");
		NodeDefinition regionDefn = clusterDefn.getChildDefinition("region");
		
		Assert.assertFalse(validator.validateCircularReferenceAbsence(clusterDefn, regionDefn, "region"));
		Assert.assertFalse(validator.validateCircularReferenceAbsence(clusterDefn, regionDefn, "region_district"));
		Assert.assertTrue(validator.validateCircularReferenceAbsence(clusterDefn, regionDefn, "district"));
	}
	
}
