package org.openforis.idm.model;

import static org.junit.Assert.assertTrue;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.attributeDef;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.entityDef;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.testfixture.NodeDefinitionBuilder;

/**
 * @author S. Ricci
 * @author D. Wiell
 */
@SuppressWarnings("unchecked")
public class RelevanceDependencyGraphTest extends NodePointerDependencyGraphTest {

	@Test
	public void testNoRelevance() {
		List<NodePointer> dependencies = determineDependents(rootEntity);
		assertTrue(dependencies.isEmpty());
	}

	@Test
	public void testSingleDependency() {
		rootEntityDef(
			attributeDef("health"),
			attributeDef("dbh")
				.relevant("health = 1")
		);
		createTestRecord();

		Attribute<?, ?> health = attribute(rootEntity, "health");
		Attribute<?, ?> dbh = attribute(rootEntity, "dbh");

		assertDependentNodePointers(health, toPointer(dbh));
	}

	@Test
	public void testSingleNestedDependency() {
		rootEntityDef(
			entityDef("details",
				attributeDef("accessibility")
			),
			entityDef("tree",
				attributeDef("dbh")
					.relevant("parent()/details/accessibility = 1")
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
		rootEntityDef(
			attributeDef("source1"),
			attributeDef("relevant1")
				.relevant("source1 = 1"),
			attributeDef("source2"),
			attributeDef("relevant2")
				.relevant("source2 = 1")
		);
		createTestRecord();

		Node<?> source1 = attribute(rootEntity, "source1");
		Node<?> relevant1 = attribute(rootEntity, "relevant1");

		Node<?> source2 = attribute(rootEntity, "source2");
		Node<?> relevant2 = attribute(rootEntity, "relevant2");

		assertDependentNodePointers(
				Arrays.asList(source1, source2),
				toPointer(relevant1),
				toPointer(relevant2));
	}

	@Test
	public void testRemoveDependent() {
		rootEntityDef(
			entityDef("details",
				attributeDef("accessibility")
			),
			entityDef("tree",
				attributeDef("dbh")
					.relevant("parent()/details/accessibility")
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
		rootEntityDef(
			entityDef("details",
					attributeDef("accessibility")
			),
			entityDef("tree",
					attributeDef("dbh")
							.relevant("parent()/details/accessibility")
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
		return record.relevanceDependencies.dependenciesFor(sources);
	}

	protected EntityDefinition rootEntityDef(NodeDefinitionBuilder... builders) {
		EntityDefinition rootEntityDef = NodeDefinitionBuilder.rootEntityDef(survey, "root", builders);
		return rootEntityDef;
	}
	
}
