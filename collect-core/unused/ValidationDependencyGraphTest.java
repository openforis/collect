package org.openforis.idm.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.attributeDef;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.entityDef;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.rootEntityDef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

/**
 * 
 * @author S. Ricci
 * @author D. Wiell
 *
 */
@SuppressWarnings({"unchecked"})
public class ValidationDependencyGraphTest extends DependencyGraphTest {

	@Test
	public void testRootEntity() {
		List<Attribute<?, ?>> dependencies = (List<Attribute<?, ?>>) determineDependents(rootEntity);
		assertTrue(dependencies.isEmpty());
	}

	@Test
	public void testOneCheck() {
		Attribute<?, ?> height = attribute(rootEntity, "height");
		Attribute<?, ?> dbh = attributeWithCheck(rootEntity, "dbh", "height > 1");
		
		assertValidationCheckDependentsInAnyOrder(height, height, dbh);
	}
	
	@Test
	public void testEntityReturnsChildrenDependents() {
		Attribute<?, ?> qty = attribute(rootEntity, "qty");
		Attribute<?, ?> total = attributeWithCheck(rootEntity, "total", "qty > 1");
		
		assertValidationCheckDependentsInAnyOrder(rootEntity, total, qty);
	}
	
	@Test
	public void testRemoveDependent() {
		Attribute<?, ?> qty = attribute(rootEntity, "qty");
		Attribute<?, ?> total = attributeWithCheck(rootEntity, "total", "qty > 1");
		
		assertValidationCheckDependentsInAnyOrder(qty, qty, total);
		
		rootEntity.remove("total", 0);
		
		assertValidationCheckDependentsInAnyOrder(qty, qty);
	}
	
	@Test
	public void testNoDependencies() {
		attribute(rootEntity, "qty");
		Attribute<?, ?> noDependencies = attribute(rootEntity, "no_dependencies");
		attributeWithCheck(rootEntity, "total", "qty > 1");

		assertDependents(noDependencies, noDependencies);
	}
	
	@Test
	public void testUniqueEntityKeys() {
		rootEntityDef(survey, "plot",
			entityDef("tree",
				attributeDef("key")
					.key()
				).multiple()
		);
		
		createTestRecord();
		
		Entity tree1 = entity(rootEntity, "tree");
		Attribute<?, ?> key1 = attribute(tree1, "key");
		
		Entity tree2 = entity(rootEntity, "tree");
		Attribute<?, ?> key2 = attribute(tree2, "key");
		
		assertValidationCheckDependentsInAnyOrder(key1, key1, key2);
		assertValidationCheckDependentsInAnyOrder(key2, key2, key1);
	}
	
	@Test
	public void testAggregateUnderTheSameEntity() {
		rootEntityDef(survey, "plot",
			entityDef("tree",
				attributeDef("value")
					.validate("sum(parent()/tree/value) > 1")
			)
		);
		
		createTestRecord();
		
		Entity tree1 = entity(rootEntity, "tree");
		Attribute<?, ?> value1 = attribute(tree1, "value");
		
		Entity tree2 = entity(rootEntity, "tree");
		Attribute<?, ?> value2 = attribute(tree2, "value");
		
		assertValidationCheckDependentsInAnyOrder(value1, value1, value2);
		assertValidationCheckDependentsInAnyOrder(value2, value2, value1);
	}
	
	@Override
	protected List<?> determineDependents(Node<?> source) {
		List<?> dependencies = source.record.validationDependencies.dependenciesFor(source);
		return dependencies;
	}
	
	@SuppressWarnings("rawtypes")
	protected void assertValidationCheckDependentsInAnyOrder(Node<?> source, Node<?>... expectedDependents) {
		List<Node<?>> sourceNodes = new ArrayList<Node<?>>();
		sourceNodes.add(source);
		List<Node<?>> dependencies = new ArrayList<Node<?>>(record.determineValidationDependentNodes(sourceNodes));
		
		assertEquals(new HashSet(toPaths(Arrays.asList(expectedDependents))), new HashSet(toPaths(dependencies)));
	}
}
