package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaTreeModel extends AbstractTreeModel<NodeDefinition> {
	
	private static final long serialVersionUID = 1L;
	
	private boolean includeAttributes;
	private ModelVersion version;

	SchemaTreeModel(NodeDefinitionTreeNode root, ModelVersion version, boolean includeAttributes) {
		super(root);
		this.includeAttributes = includeAttributes;
		this.version = version;
	}
	
	public static SchemaTreeModel createInstance(CollectSurvey survey, boolean includeAttributes) {
		return createInstance(survey, null, includeAttributes);
	}
	
	public static SchemaTreeModel createInstance(EntityDefinition rootEntity, ModelVersion version, boolean includeAttributes) {
		if ( rootEntity != null && (version == null || version.isApplicable(rootEntity)) ) {
			List<AbstractTreeNode<NodeDefinition>> treeNodes = NodeDefinitionTreeNode.fromList(rootEntity.getChildDefinitions(), version, includeAttributes);
			NodeDefinitionTreeNode root = new NodeDefinitionTreeNode(null, treeNodes);
			SchemaTreeModel result = new SchemaTreeModel(root, version, includeAttributes);
			return result;
		} else {
			return null;
		}
	}
	
	public static SchemaTreeModel createInstance(CollectSurvey survey, ModelVersion version, boolean includeAttributes) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootDefns = schema.getRootEntityDefinitions();
		List<AbstractTreeNode<NodeDefinition>> treeNodes = NodeDefinitionTreeNode.fromList(rootDefns, version, includeAttributes);
		NodeDefinitionTreeNode root = new NodeDefinitionTreeNode(null, treeNodes);
		SchemaTreeModel result = new SchemaTreeModel(root, version, includeAttributes);
		return result;
	}
	
	@Override
	protected AbstractTreeNode<NodeDefinition> createNode(NodeDefinition data) {
		NodeDefinitionTreeNode result = NodeDefinitionTreeNode.createNode(data, version, includeAttributes);
		return result;
	}
	
	@Override
	protected int[] getNodePath(NodeDefinition defn) {
		if ( defn != null ) {
			EntityDefinition parent = (EntityDefinition) defn.getParentDefinition();
			NodeDefinition current = defn;
			List<Integer> temp = new ArrayList<Integer>();
			int index;
			while ( parent != null ) {
				index = getIndexInTree(parent, current);
				temp.add(0, index);
				current = parent;
				parent = (EntityDefinition) current.getParentDefinition();
			}
			EntityDefinition rootEntity = current.getRootEntity();
			Schema schema = rootEntity.getSchema();
			index = schema.getRootEntityIndex(rootEntity);
			temp.add(0, index);
			int[] result = toArray(temp);
			return result;
		} else {
			return null;
		}
	}

	protected int getIndexInTree(EntityDefinition parent, NodeDefinition node) {
		if ( includeAttributes ) {
			return parent.getChildIndex(node);
		} else {
			List<NodeDefinition> childDefns = parent.getChildDefinitions();
			List<EntityDefinition> siblingEtities = new ArrayList<EntityDefinition>();
			for (NodeDefinition childDefn : childDefns) {
				if ( childDefn instanceof EntityDefinition ) {
					siblingEtities.add((EntityDefinition) childDefn);
				}
			}
			return siblingEtities.indexOf(node);
		}
	}

	protected NodeDefinitionTreeNode recreateNode(NodeDefinitionTreeNode node) {
		NodeDefinitionTreeNode parent = (NodeDefinitionTreeNode) node.getParent();
		NodeDefinition data = node.getData();
		NodeDefinitionTreeNode newNode = NodeDefinitionTreeNode.createNode(data, version, includeAttributes);
		parent.replace(node, newNode);
		return newNode;
	}

	static class NodeDefinitionTreeNode extends AbstractTreeNode<NodeDefinition> {
		
		private static final long serialVersionUID = 1L;
		
		NodeDefinitionTreeNode(AttributeDefinition data) {
			super(data);
		}
		
		NodeDefinitionTreeNode(EntityDefinition data) {
			super(data);
		}
		
		NodeDefinitionTreeNode(EntityDefinition data, Collection<AbstractTreeNode<NodeDefinition>> children) {
			super(data, children);
		}
		
		public static NodeDefinitionTreeNode createNode(NodeDefinition item, ModelVersion version,
				boolean includeAttributes) {
			NodeDefinitionTreeNode node = null;
			if ( item instanceof EntityDefinition ) {
				List<NodeDefinition> childDefns = ((EntityDefinition) item).getChildDefinitions();
				List<AbstractTreeNode<NodeDefinition>> childNodes = fromList(childDefns, version, includeAttributes);
				if ( childNodes == null || childNodes.isEmpty() ) {
					node = new NodeDefinitionTreeNode((EntityDefinition) item);
				} else {
					node = new NodeDefinitionTreeNode((EntityDefinition) item, childNodes);
				}
			} else if ( includeAttributes ) {
				node = new NodeDefinitionTreeNode((AttributeDefinition) item);	
			}
			return node;
		}
		
		public static List<AbstractTreeNode<NodeDefinition>> fromList(List<? extends NodeDefinition> items,
				ModelVersion version, boolean includeAttributes) {
			List<AbstractTreeNode<NodeDefinition>> result = null;
			if ( items != null ) {
				result = new ArrayList<AbstractTreeNode<NodeDefinition>>();
				for (NodeDefinition item : items) {
					if ( version == null || version.isApplicable(item) ) {
						NodeDefinitionTreeNode node = createNode(item, version, includeAttributes);
						if ( node != null ) {
							result.add(node);
						}
					}
				}
			}
			return result;
		}

	}

}
