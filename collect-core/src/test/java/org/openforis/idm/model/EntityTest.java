package org.openforis.idm.model;

import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.openforis.idm.AbstractTest;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class EntityTest extends AbstractTest {

	@Test
	public void testAddNullCode() {
		Entity cluster = getRootEntity();
		EntityBuilder.addValue(cluster, "id", (Code) null);
		CodeAttribute clusterId = cluster.getChild("id");
		assertNull(clusterId.getValue().getCode());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddAttributeOnEntity() {
		Entity cluster = getRootEntity();
		EntityBuilder.addValue(cluster, "plot", new Code("123_456"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddEntityOnAttribute() {
		Entity cluster = getRootEntity();
		EntityBuilder.addEntity(cluster, "id");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddUndefinedEntity() {
		Entity cluster = getRootEntity();
		EntityBuilder.addEntity(cluster, "xxx");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddUndefinedAttribute() {
		Entity cluster = getRootEntity();
		EntityBuilder.addValue(cluster, "xxx", 2.0);
	}

//	@Test
//	public void testValidateRootEntity() {
//		Entity cluster = getRootEntity();
//		State nodeState = new State(cluster);
//		ValidationResults results = new Validator().validate(nodeState);
//		int errors = results.getErrors().size();
//		Assert.assertEquals(5, errors);
//	}

//	@Test
//	public void testValidatePlot() {
//		Entity cluster = getRootEntity();
//		Entity plot = EntityBuilder.addEntity(cluster, "plot");
//		EntityBuilder.addValue(plot, "share", 20.0);
//		
//		State nodeState = new State(plot);
//		ValidationResults results = new Validator().validate(nodeState);
//		int errors = results.getErrors().size();
//		Assert.assertEquals(16, errors);
//	}

	private Entity getRootEntity() {
		Record record = new Record(survey, "2.0", "cluster");
		return record.getRootEntity();
	}
}
