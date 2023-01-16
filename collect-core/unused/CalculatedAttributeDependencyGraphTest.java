package org.openforis.idm.model;

import static junit.framework.Assert.assertTrue;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.attributeDef;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.entityDef;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.rootEntityDef;

import java.util.List;

import org.junit.Test;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 * @author D. Wiell
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CalculatedAttributeDependencyGraphTest extends DependencyGraphTest {

	@Test
	public void testRootEntity() {
		List<Attribute<?, ?>> dependencies = (List<Attribute<?, ?>>) determineDependents(rootEntity);
		assertTrue(dependencies.isEmpty());
	}

	@Test
	public void testOneCalculatedAttribute() {
		Attribute<?, ?> qty = attribute(rootEntity, "qty");
		Attribute<?, ?> total = calculatedAttribute(rootEntity, "total", "qty");

		assertDependents(qty, total);
	}

	@Test
	public void testEntityReturnsChildrenDependents() {
		attribute(rootEntity, "qty");
		Attribute<?, ?> total = calculatedAttribute(rootEntity, "total", "qty");

		assertDependents(rootEntity, total);
	}

	@Test
	public void testRemoveDependent() {
		Attribute<?, ?> qty = attribute(rootEntity, "qty");
		Attribute<?, ?> total = calculatedAttribute(rootEntity, "total", "qty");

		assertDependents(qty, total);

		rootEntity.remove("total", 0);

		assertDependents(qty);
	}

	@Test
	public void testNoDependencies() {
		attribute(rootEntity, "qty");
		Attribute<?, ?> noDependencies = attribute(rootEntity, "no_dependencies");
		calculatedAttribute(rootEntity, "total", "qty");

		assertDependents(noDependencies);
	}

	@Test
	public void testCrossEntityDependency() {
		Entity plot = entity(rootEntity, "plot");
		Entity plotDetails = entity(plot, "plot_details");
		Attribute<?, ?> plotNoAttr = attribute(plotDetails, "plot_no");
		Entity tree = entity(plot, "tree");
		Attribute<?, ?> calculatedAttr = calculatedAttribute(tree, "calculated", "parent()/plot_details/plot_no + 1");

		assertDependents(plotNoAttr, calculatedAttr);
	}

	@Test
	public void testComplexCrossEntityDependency() {
		Entity plot = entity(rootEntity, "plot");
		Entity lucs = entity(plot, "lucs");
		Attribute<?, ?> lucsNo = attribute(lucs, "lucs_no");
		Attribute<?, ?> lucc = attribute(lucs, "lucc");
		Entity tree = entity(plot, "tree");
		Attribute<?, ?> treeLucsNo = attribute(tree, "tree_lucs_no");
		Attribute<?, ?> calculatedAttr = calculatedAttribute(tree, "calculated", "parent()/lucs[lucs_no = $this/parent()/tree_lucs_no]/lucc");
		
		assertDependents(lucsNo, calculatedAttr);
		assertDependents(treeLucsNo, calculatedAttr);
		assertDependents(lucc, calculatedAttr);
	}

	@Test
	public void testMultipleSources() {
		Attribute<?, ?> first = attribute(rootEntity, "first");
		Attribute<?, ?> second = attribute(rootEntity, "second");
		Attribute<?, ?> total = calculatedAttribute(rootEntity, "total", "first + second");

		assertDependents(first, total);
		assertDependents(second, total);
	}

	@Test
	public void testMultipleDependents() {
		Attribute<?, ?> qty = attribute(rootEntity, "qty");
		Attribute<?, ?> total1 = calculatedAttribute(rootEntity, "total1", "qty + 1");
		Attribute<?, ?> total2 = calculatedAttribute(rootEntity, "total2", "qty + 2");

		assertCalculatedDependentsInAnyOrder(qty, total1, total2);
	}

	@Test
	public void testChainDependents() {
		Attribute<?, ?> qty = attribute(rootEntity, "qty");
		Attribute<?, ?> total1 = calculatedAttribute(rootEntity, "total1", "qty + 1");
		Attribute<?, ?> total2 = calculatedAttribute(rootEntity, "total2", "total1 + 2");

		assertDependents(qty, total1, total2);
	}

	@Test
	public void testAddingDependentAfterSource() {
		EntityDefinition rootEntityDef = rootEntity.getDefinition();

		attributeDefinition(rootEntityDef, "source");
		calculatedAttributeDefinition(rootEntityDef, "dependent", "source");

		Attribute<?, ?> dependent = attribute(rootEntity, "dependent");
		Attribute<?, ?> source = attribute(rootEntity, "source");

		assertDependents(source, dependent);
	}

	@Test
	public void testChainWithMultiplePaths() {
		EntityDefinition rootEntityDef = rootEntity.getDefinition();

		Attribute<?, ?> qty = attribute(rootEntity, "qty");

		calculatedAttributeDefinition(rootEntityDef, "t1", "qty + 1");
		calculatedAttributeDefinition(rootEntityDef, "t2", "t1 + qty");

		Attribute<?, ?> t2 = calculatedAttribute(rootEntity, "t2", "qty + t1");
		Attribute<?, ?> t1 = calculatedAttribute(rootEntity, "t1", "qty + 1");

		assertDependents(qty, t1, t2);
	}

	@Test
	public void testAggregateFunctions() {
		Entity plot = entity(rootEntity, "plot");

		EntityDefinition treeDef = entityDefinition(plot.getDefinition(), "tree");
		attributeDefinition(treeDef, "dbh");

		Entity tree1 = entity(plot, "tree");
		Attribute<?, ?> dbh1 = attribute(tree1, "dbh");
		Entity tree2 = entity(plot, "tree");
		Attribute<?, ?> dbh2 = attribute(tree2, "dbh");

		Attribute<?, ?> total = calculatedAttribute(plot, "total", "sum(tree/dbh)");

		assertDependents(dbh1, total);
		assertDependents(dbh2, total);
	}
	
	@Test
	public void testDependencyOnEntityCount() {
		Entity plot = entity(rootEntity, "plot");

		entityDefinition(plot.getDefinition(), "tree");
		
		Attribute<?, ?> treeCount = calculatedAttribute(plot, "tree_count", "count(tree)");
		Entity tree1 = entity(plot, "tree");
		Entity tree2 = entity(plot, "tree");
		
		assertDependents(tree1, treeCount);
		assertDependents(tree2, treeCount);
	}

	@Test
	public void testAggregateFunctionsInSameEntity() {
		Entity plot = entity(rootEntity, "plot");

		EntityDefinition treeDef = entityDefinition(plot.getDefinition(), "tree");
		attributeDefinition(treeDef, "dbh");

		Entity tree1 = entity(plot, "tree");
		Attribute<?, ?> dbh1 = attribute(tree1, "dbh");
		Attribute<?, ?> dbhsum1 = calculatedAttribute(tree1, "dbhsum", "sum(parent()/tree/dbh)");

		Entity tree2 = entity(plot, "tree");
		Attribute<?, ?> dbh2 = attribute(tree2, "dbh");
		Attribute<?, ?> dbhsum2 = calculatedAttribute(tree2, "dbhsum", "sum(parent()/tree/dbh)");

		assertCalculatedDependentsInAnyOrder(dbh1, dbhsum1, dbhsum2);
		assertCalculatedDependentsInAnyOrder(dbh2, dbhsum1, dbhsum2);
	}

	@Test
	public void testAggregateFunctionWithFilter() {
		Entity plot = entity(rootEntity, "plot");

		EntityDefinition treeDef = entityDefinition(plot.getDefinition(), "tree");
		attributeDefinition(treeDef, "dbh");

		Entity tree1 = entity(plot, "tree");
		Attribute<?, ?> dbh1 = attribute(tree1, "dbh");
		Entity tree2 = entity(plot, "tree");
		Attribute<?, ?> dbh2 = attribute(tree2, "dbh");

		Attribute<?, ?> total = calculatedAttribute(plot, "total", "sum(tree[idm:position() = 2]/dbh)");

		assertDependents(dbh1, total);
		assertDependents(dbh2, total);
	}

	@Test
	public void testRemoveEntityWithAgregateFunction() {
		Entity plot = entity(rootEntity, "plot");

		EntityDefinition treeDef = entityDefinition(plot.getDefinition(), "tree");
		attributeDefinition(treeDef, "dbh");

		Entity tree1 = entity(plot, "tree");
		Attribute<?, ?> dbh1 = attribute(tree1, "dbh");
		Attribute<?, ?> dbhsum1 = calculatedAttribute(tree1, "dbhsum", "sum(parent()/tree/dbh)");

		Entity tree2 = entity(plot, "tree");
		Attribute<?, ?> dbh2 = attribute(tree2, "dbh");
		Attribute<?, ?> dbhsum2 = calculatedAttribute(tree2, "dbhsum", "sum(parent()/tree/dbh)");

		assertCalculatedDependentsInAnyOrder(dbh1, dbhsum1, dbhsum2);
		assertCalculatedDependentsInAnyOrder(dbh2, dbhsum1, dbhsum2);

		plot.remove("tree", 0);

		assertCalculatedDependentsInAnyOrder(dbh2, dbhsum2);
	}

	@Test
	public void testAddDetachedEntity() {
		EntityDefinition plotDef = entityDefinition(rootEntity.getDefinition(), "plot");
		Entity plot = (Entity) plotDef.createNode();

		Attribute<?, ?> plotNo = attribute(plot, "plot_no");
		Attribute<?, ?> total = calculatedAttribute(plot, "total", "plot_no + 1");

		rootEntity.add(plot);

		assertDependents(plotNo, total);
	}

	@Test
	public void testAllCalculatedAttributes() {
		attribute(rootEntity, "a");
		Attribute<?, ?> t1 = calculatedAttribute(rootEntity, "t1", "a + 1");
		Attribute<?, ?> t2 = calculatedAttribute(rootEntity, "t2", "t1 + 2");

		attribute(rootEntity, "b");
		Attribute<?, ?> z1 = calculatedAttribute(rootEntity, "z1", "b + 1");
		Attribute<?, ?> z2 = calculatedAttribute(rootEntity, "z2", "z1 + 2");

		List attributes = record.determineCalculatedAttributes(rootEntity);
		assertBefore(attributes, t1, t2);
		assertBefore(attributes, z1, z2);
	}

	@Test
	public void testAggregateUnderTheSameEntity() {
		rootEntityDef(survey, "root",
				entityDef("tree",
						attributeDef("value"),
						attributeDef("sum")
								.calculated("sum(parent()/tree/value)")
				).multiple()
		);

		createTestRecord();

		Entity tree1 = entity(rootEntity, "tree");
		Attribute<?, ?> value1 = attribute(tree1, "value");
		Attribute<?, ?> sum1 = attribute(tree1, "sum");

		Entity tree2 = entity(rootEntity, "tree");
		Attribute<?, ?> value2 = attribute(tree2, "value");
		Attribute<?, ?> sum2 = attribute(tree2, "sum");

		assertCalculatedDependentsInAnyOrder(value1, sum1, sum2);
		assertCalculatedDependentsInAnyOrder(value2, sum1, sum2);
	}

	@Test
	public void testConditionEvaluation() {
		rootEntityDef(survey, "root",
				attributeDef("region"),
				entityDef("tree",
						attributeDef("value")
							.calculated("parent()/region"),
						attributeDef("sum")
							.calculated("sum(parent()/tree/value)", "value = '001'")
				).multiple()
		);

		createTestRecord();

		Attribute<?, ?> region = attribute(rootEntity, "region");
		
		Entity tree1 = entity(rootEntity, "tree");
		Attribute<?, ?> value1 = attribute(tree1, "value");
		Attribute<?, ?> sum1 = attribute(tree1, "sum");

		Entity tree2 = entity(rootEntity, "tree");
		Attribute<?, ?> value2 = attribute(tree2, "value");
		Attribute<?, ?> sum2 = attribute(tree2, "sum");

		assertCalculatedDependentsInAnyOrder(region, value1, value2, sum1, sum2);
	}

	@Test(expected=IllegalStateException.class)
	public void testCalculatedAttributeCircularDependency() {
		rootEntityDef(survey, "root",
			attributeDef("c1")
				.calculated("c2")
			, attributeDef("c2")
				.calculated("c1")
		);
		createTestRecord();
		attribute(rootEntity, "c1");
		attribute(rootEntity, "c2");
		record.determineCalculatedAttributes(rootEntity);
	}
	
	@Override
	protected List<?> determineDependents(Node<?> source) {
		List<?> dependencies = source.getRecord().determineCalculatedAttributes(source);
		return dependencies;
	}


}
