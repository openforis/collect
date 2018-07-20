package org.openforis.idm.model;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.attributeDef;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.entityDef;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.rootEntityDef;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
/**
 * 
 * @author S. Ricci
 * @author D. Wiell
 *
 */
public class MinCountDependencyGraphTest extends NodePointerDependencyGraphTest {

	@Test
	public void testNoRequiness() {
		List<NodePointer> dependencies = determineDependents(rootEntity);
		assertTrue(dependencies.isEmpty());
	}
	
	@Test
	public void testSingleDependency() {
		rootEntityDef(survey, "root",
			attributeDef("health"),
			attributeDef("dbh")
				.required("health = 1")
		);
		createTestRecord();
		
		Attribute<?, ?> health = attribute(rootEntity, "health");
		Attribute<?, ?> dbh = attribute(rootEntity, "dbh");
		
		assertDependentNodePointers(health, toPointer(dbh));
	}
	
	@Test
	public void testSingleNestedDependency() {
		rootEntityDef(survey, "plot",
			entityDef("details",
				attributeDef("accessibility")
			),
			entityDef("tree",
				attributeDef("dbh")
					.required("parent()/details/accessibility = 1")
				)
		);
	
		createTestRecord();

		Entity details = entity(rootEntity, "details");
		Attribute<?, ?> accessibility = attribute(details, "accessibility");
		
		Entity tree = entity(rootEntity, "tree");
		Attribute<?, ?> dbh = attribute(tree, "dbh");
		
		assertDependentNodePointers(accessibility, toPointer(dbh));
	}
	
	@Test
	public void testDependencyWithMultipleSources() {
		rootEntityDef(survey, "root",
			attributeDef("source1"),
			attributeDef("required1")
				.required("source1 = 1"),
			attributeDef("source2"),
			attributeDef("required2")
				.required("source2 = 1")
		);
		createTestRecord();
		
		Node<?> source1 = attribute(rootEntity, "source1");
		Node<?> required1 = attribute(rootEntity, "required1");

		Node<?> source2 = attribute(rootEntity, "source2");
		Node<?> required2 = attribute(rootEntity, "required2");

		assertRequiredDependents(
				Arrays.asList(toPointer(source1), toPointer(source2)), 
					toPointer(required1), 
					toPointer(required2));
	}

	@Test
	public void testRemoveDependent() {
		rootEntityDef(survey, "root",
			entityDef("details",
				attributeDef("accessibility")
			),
			entityDef("tree",
				attributeDef("dbh")
					.required("parent()/details/accessibility")
			)
		);
		
		createTestRecord();
		
		Entity details = entity(rootEntity, "details");
		Attribute<?, ?> accessibility = attribute(details, "accessibility");

		Entity tree = entity(rootEntity, "tree");
		Attribute<?, ?> dbh = attribute(tree, "dbh");
		
		NodePointer dbhPointer = toPointer(dbh);
		
		assertDependentNodePointers(accessibility, dbhPointer);
		
		tree.remove("dbh", 0);
		
		assertDependentNodePointers(accessibility, dbhPointer);
		
		rootEntity.remove("tree", 0);
		
		assertDependentNodePointers(accessibility);
	}
	
	@Test
	public void testRemoveDependentInMultipleEntity() {
		rootEntityDef(survey, "root",
			entityDef("details",
				attributeDef("accessibility")
			),
			entityDef("tree",
				attributeDef("dbh")
					.required("parent()/details/accessibility")
			).multiple()
		);
		
		createTestRecord();
		
		Entity details = entity(rootEntity, "details");
		Attribute<?, ?> accessibility = attribute(details, "accessibility");

		Entity tree1 = entity(rootEntity, "tree");
		Attribute<?, ?> dbh1 = attribute(tree1, "dbh");
		NodePointer dbh1Pointer = toPointer(dbh1);
		
		Entity tree2 = entity(rootEntity, "tree");
		Attribute<?, ?> dbh2 = attribute(tree2, "dbh");
		NodePointer dbh2Pointer = toPointer(dbh2);
		
		assertDependentNodePointers(accessibility, dbh1Pointer, dbh2Pointer);
		
		rootEntity.remove("tree", 0);
		
		assertDependentNodePointers(accessibility, dbh2Pointer);
	}

	@Override
	protected List<NodePointer> determineDependents(List<Node<?>> sources) {
		return record.minCountDependencies.dependenciesFor(sources);
	}
	
	protected void assertRequiredDependents(List<NodePointer> sources, NodePointer... expectedDependents) {
		List<NodePointer> dependents = record.minCountDependencies.dependenciesForPointers(sources);
		assertEquals(toPointerPaths(Arrays.asList(expectedDependents)), toPointerPaths(dependents));
	}
	
}
