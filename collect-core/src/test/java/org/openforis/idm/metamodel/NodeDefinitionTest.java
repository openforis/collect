package org.openforis.idm.metamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.openforis.idm.AbstractTest;

/**
 * @author G. Miceli
 */
public class NodeDefinitionTest extends AbstractTest {

	@Test
	public void testGetPathAtRoot() {
		NodeDefinition mock = mock(NodeDefinition.class);
		doCallRealMethod().when(mock).getName();
		doCallRealMethod().when(mock).getPath();
		doCallRealMethod().when(mock).setName(anyString());
		doCallRealMethod().when(mock).updatePath();
		mock.setName("cluster");
		assertEquals("/cluster", mock.getPath());
	}

	@Test
	public void testGetPathAtSecondLevel() {
		EntityDefinition parentMock = mock(EntityDefinition.class);
		doCallRealMethod().when(parentMock).getName();
		doCallRealMethod().when(parentMock).getPath();
		doCallRealMethod().when(parentMock).setName(anyString());
		doCallRealMethod().when(parentMock).updatePath();
		parentMock.setName("cluster");

		NodeDefinition mock = mock(NodeDefinition.class);
		doCallRealMethod().when(mock).getName();
		doCallRealMethod().when(mock).getPath();
		doCallRealMethod().when(mock).setName(anyString());
		doCallRealMethod().when(mock).updatePath();
		doCallRealMethod().when(mock).getParentDefinition();
		doCallRealMethod().when(mock).setParentDefinition(any(NodeDefinition.class));

		mock.setName("plot");
		mock.setParentDefinition(parentMock);
		assertEquals("/cluster/plot", mock.getPath());
	}
	
	@Test
	public void testRootEntityDefinitionIsMultiple() {
		Schema schema = survey.getSchema();
		EntityDefinition clusterDefn = schema.getRootEntityDefinition("cluster");
		assertTrue(clusterDefn.isMultiple());
	}
}
