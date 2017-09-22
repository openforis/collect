package org.openforis.idm.path;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.idm.AbstractTest;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.Record;

/**
 * @author G. Miceli
 */
public class PathTest extends AbstractTest {

	@Test
	public void testSingleAttributeWithIndex() throws InvalidPathException {
		Entity cluster = getRootEntity();
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree1 = EntityBuilder.addEntity(plot, "tree");
		EntityBuilder.addValue(tree1, "dbh", 12.2);
		Entity tree2 = EntityBuilder.addEntity(plot, "tree");
		RealAttribute dbh2 = EntityBuilder.addValue(tree2, "dbh", 15.7);
		
		Path path = Path.parse("tree[2]/dbh[1]");
		
		// Node
		List<Node<?>> res = path.evaluate(plot);
		Assert.assertEquals(1, res.size());
		Assert.assertEquals(dbh2, res.get(0));
		
		// Defn
		NodeDefinition def = path.evaluate(plot.getDefinition());
		Assert.assertEquals(dbh2.getDefinition(), def);
	}

	@Test
	public void testSingleAttributeWithoutIndex() throws InvalidPathException {
		Entity cluster = getRootEntity();
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree1 = EntityBuilder.addEntity(plot, "tree");
		EntityBuilder.addValue(tree1, "dbh", 12.2);
		Entity tree2 = EntityBuilder.addEntity(plot, "tree");
		RealAttribute dbh2 = EntityBuilder.addValue(tree2, "dbh", 15.7);
		
		Path path = Path.parse("tree[2]/dbh");
		
		// Node
		List<Node<?>> res = path.evaluate(plot);
		Assert.assertEquals(1, res.size());
		Assert.assertEquals(dbh2, res.get(0));
		
		// Defn
		NodeDefinition def = path.evaluate(plot.getDefinition());
		Assert.assertEquals(dbh2.getDefinition(), def);
	}

	@Test
	public void testMultipleAttributeWithoutIndex() throws InvalidPathException {
		Entity cluster = getRootEntity();
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addEntity(plot, "tree");
		Entity tree2 = EntityBuilder.addEntity(plot, "tree");
		RealAttribute dbh1 = EntityBuilder.addValue(tree2, "dbh", 12.2);
		RealAttribute dbh2 = EntityBuilder.addValue(tree2, "dbh", 15.7);
		
		Path path = Path.parse("tree[2]/dbh");
		
		// Node
		List<Node<?>> res = path.evaluate(plot);
		Assert.assertEquals(2, res.size());
		Assert.assertEquals(dbh1, res.get(0));
		Assert.assertEquals(dbh2, res.get(1));
		
		// Defn
		NodeDefinition def = path.evaluate(plot.getDefinition());
		Assert.assertEquals(dbh2.getDefinition(), def);
	}

	@Test
	public void testMultipleFieldPathWithIndex() throws InvalidPathException {
		Entity cluster = getRootEntity();
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addEntity(plot, "tree");
		Entity tree2 = EntityBuilder.addEntity(plot, "tree");
		EntityBuilder.addValue(tree2, "dbh", 12.2);
		RealAttribute dbh2 = EntityBuilder.addValue(tree2, "dbh", 15.7);
		
		Path path = Path.parse("tree[2]/dbh[2]/value");
		
		// Node
		List<Node<?>> res = path.evaluate(plot);
		Assert.assertEquals(1, res.size());
		Assert.assertEquals(15.7, ((Field<?>)res.get(0)).getValue());
		
		// Defn
		NodeDefinition def = path.evaluate(plot.getDefinition());
		Assert.assertEquals(dbh2.getDefinition().getFieldDefinition("value"), def);
	}

	@Test
	public void testMultipleFieldPathWithoutIndex() throws InvalidPathException {
		Entity cluster = getRootEntity();
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addEntity(plot, "tree");
		Entity tree2 = EntityBuilder.addEntity(plot, "tree");
		RealAttribute dbh1 = EntityBuilder.addValue(tree2, "dbh", 12.2);
		RealAttribute dbh2 = EntityBuilder.addValue(tree2, "dbh", 15.7);
		
		Path path = Path.parse("tree[2]/dbh/value");
		
		// Node
		List<Node<?>> res = path.evaluate(plot);
		Assert.assertEquals(2, res.size());
		Assert.assertEquals(12.2, ((Field<?>)res.get(0)).getValue());
		Assert.assertEquals(15.7, ((Field<?>)res.get(1)).getValue());
		
		// Defn
		NodeDefinition def = path.evaluate(plot.getDefinition());
		Assert.assertEquals(dbh1.getDefinition().getFieldDefinition("value"), def);
		Assert.assertEquals(dbh2.getDefinition().getFieldDefinition("value"), def);
	}

	private Entity getRootEntity() {
		Record record = new Record(survey, "2.0", "cluster");
		return record.getRootEntity();
	}
}
