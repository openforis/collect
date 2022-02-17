package org.openforis.idm.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.idm.metamodel.NodeDefinition;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class NodePointerDependencyGraphTest extends
		DependencyGraphTest {

	public NodePointerDependencyGraphTest() {
		super();
	}

	protected NodePointer toPointer(Node<?> node) {
		return new NodePointer(node);
	}

	@Override
	protected void assertDependents(Node<?> source, Node<?>... expectedDependents) {
		List<?> dependencies = determineDependents(source);
		
		assertEquals(toPaths(Arrays.asList(expectedDependents)), toPointerPaths((List<NodePointer>) dependencies));
	}

	protected void assertDependentNodePointers(Node<?> source, NodePointer... expectedDependents) {
		List<?> dependencies = determineDependents(source);
		Set<String> expectedPaths = toPointerPaths(Arrays.asList(expectedDependents));
		Set<String> actualPaths = toPointerPaths((List<NodePointer>) dependencies);
		assertEquals(expectedPaths, actualPaths);
	}

	protected void assertDependentNodePointers(List<Node<? extends NodeDefinition>> sources, NodePointer... expectedDependents) {
		Collection<NodePointer> dependents = determineDependents(sources);
		assertEquals(toPointerPaths(Arrays.asList(expectedDependents)), toPointerPaths(dependents));
	}

	@Override
	protected List<NodePointer> determineDependents(Node<?> source) {
		List<?> sources = Arrays.asList(source);
		return determineDependents((List<Node<?>>) sources);
	}

	protected void assertCalculatedDependentsInAnyOrder(Node<?> source, Node<?>... expectedDependents) {
		List sources = Arrays.asList(source);
		assertDependentsInAnyOrder(sources, expectedDependents);
	}

	protected void assertDependentsInAnyOrder(List<Node<?>> sources, Node<?>... expectedDependents) {
		Collection<NodePointer> dependents = determineDependents(sources);
		assertEquals(new HashSet(toPaths(Arrays.asList(expectedDependents))), new HashSet(toPointerPaths(dependents)));
	}

	protected abstract List<NodePointer> determineDependents(List<Node<?>> sources);

	protected Set<String> toPointerPaths(Collection<NodePointer> pointers) {
		Set<String> paths = new HashSet<String>();
		for (NodePointer pointer : pointers) {
			paths.add(pointer.getEntityPath() + "/" + pointer.getChildName() + "[1]");
		}
		return paths;
	}

}