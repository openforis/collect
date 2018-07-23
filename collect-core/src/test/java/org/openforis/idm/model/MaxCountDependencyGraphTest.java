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
public class MaxCountDependencyGraphTest extends NodePointerDependencyGraphTest {

	@Test
	public void testNoRequiness() {
		List<NodePointer> dependencies = determineDependents(rootEntity);
		assertTrue(dependencies.isEmpty());
	}
	
	@Test
	public void testSingleDependency() {
		rootEntityDef(survey, "root",
			attributeDef("max_num"),
			attributeDef("tree")
				.maxCount("number(max_num)")
		);
		createTestRecord();
		
		Attribute<?, ?> maxNum = attribute(rootEntity, "max_num");
		Entity tree = entity(rootEntity, "tree");
		
		assertDependentNodePointers(maxNum, toPointer(tree));
	}
	
	@Test
	public void testDependencyWithMultipleSources() {
		rootEntityDef(survey, "root",
			attributeDef("source1"),
			attributeDef("source2"),
			attributeDef("limited")
				.maxCount("source1 + source2")
		);
		createTestRecord();
		
		Node<?> source1 = attribute(rootEntity, "source1");
		Node<?> source2 = attribute(rootEntity, "source2");

		Node<?> limited = attribute(rootEntity, "limited");

		assertRequiredDependents(
				Arrays.asList(toPointer(source1), toPointer(source2)), 
					toPointer(limited));
	}

	@Test
	public void testRemoveDependent() {
		rootEntityDef(survey, "root",
			entityDef("details",
				attributeDef("max_measures")
			),
			entityDef("tree",
				attributeDef("measure")
					.maxCount("parent()/details/max_measures")
			)
		);
		
		createTestRecord();
		
		Entity details = entity(rootEntity, "details");
		Attribute<?, ?> maxMeasures = attribute(details, "max_measures");

		Entity tree = entity(rootEntity, "tree");
		Attribute<?, ?> measure = attribute(tree, "measure");
		
		NodePointer measurePointer = toPointer(measure);
		
		assertDependentNodePointers(maxMeasures, measurePointer);
		
		tree.remove("measure", 0);
		
		assertDependentNodePointers(maxMeasures, measurePointer);
		
		rootEntity.remove("tree", 0);
		
		assertDependentNodePointers(maxMeasures);
	}
	
	@Test
	public void testRemoveDependentInMultipleEntity() {
		rootEntityDef(survey, "root",
			entityDef("details",
				attributeDef("max_measures")
			),
			entityDef("tree",
				attributeDef("measure")
					.maxCount("parent()/details/max_measures")
			).multiple()
		);
		
		createTestRecord();
		
		Entity details = entity(rootEntity, "details");
		Attribute<?, ?> maxMeasures = attribute(details, "max_measures");

		Entity tree1 = entity(rootEntity, "tree");
		Attribute<?, ?> measure1 = attribute(tree1, "measure");
		NodePointer measure1Pointer = toPointer(measure1);
		
		Entity tree2 = entity(rootEntity, "tree");
		Attribute<?, ?> measure2 = attribute(tree2, "measure");
		NodePointer measure2Pointer = toPointer(measure2);
		
		assertDependentNodePointers(maxMeasures, measure1Pointer, measure2Pointer);
		
		rootEntity.remove("tree", 0);
		
		assertDependentNodePointers(maxMeasures, measure2Pointer);
	}

	@Override
	protected List<NodePointer> determineDependents(List<Node<?>> sources) {
		return record.maxCountDependencies.dependenciesFor(sources);
	}
	
	protected void assertRequiredDependents(List<NodePointer> sources, NodePointer... expectedDependents) {
		List<NodePointer> dependents = record.maxCountDependencies.dependenciesForPointers(sources);
		assertEquals(toPointerPaths(Arrays.asList(expectedDependents)), toPointerPaths(dependents));
	}
	
}
