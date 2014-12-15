/**
 * 
 */
package org.openforis.idm.metamodel;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openforis.idm.AbstractTest;
import org.openforis.idm.metamodel.expression.SchemaPathExpression;

/**
 * @author M. Togna
 * 
 */
public class SchemaExpressionTest extends AbstractTest {

	@Test
	public void testRootEntityDefinition() {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		EntityDefinition cluster = rootEntityDefinitions.get(0);
		assertEquals("cluster", cluster.getName());
	}

	@Test
	public void testExpression() {
		EntityDefinition cluster = survey.getSchema().getRootEntityDefinitions().get(0);

		SchemaPathExpression expression = new SchemaPathExpression("plot/tree");
		Object obj = expression.evaluate(cluster);
		assertEquals(EntityDefinition.class, obj.getClass());

		EntityDefinition tree = (EntityDefinition) obj;
		assertEquals("tree", tree.getName());
	}
	/*
	 * @Test public void testSODGetMethod() { EntityDefinition cluster = survey.getSchema().getRootEntityDefinitions().get(0); EntityDefinition tree =
	 * (EntityDefinition) cluster.getAll("plot/tree"); assertEquals("tree", tree.getName()); }
	 * 
	 * @Test public void testSchemaGetMethod() { Schema schema = survey.getSchema(); EntityDefinition tree = (EntityDefinition)
	 * schema.getAll("cluster/plot/tree"); assertEquals("tree", tree.getName()); }
	 * 
	 * @Test public void testParent() { EntityDefinition cluster = survey.getSchema().getRootEntityDefinitions().get(0); EntityDefinition tree =
	 * (EntityDefinition) cluster.getAll("plot/tree"); assertEquals("tree", tree.getName());
	 * 
	 * EntityDefinition plot = (EntityDefinition) tree.getAll("parent()"); assertEquals("plot", plot.getName());
	 * 
	 * EntityDefinition cluster1 = (EntityDefinition) tree.getAll("parent()/parent()"); assertEquals("cluster", cluster1.getName()); }
	 */
}
