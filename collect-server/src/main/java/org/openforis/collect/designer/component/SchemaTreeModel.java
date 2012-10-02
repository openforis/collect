package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeNode;

public class SchemaTreeModel extends DefaultTreeModel<NodeDefinition> {
	
	private static final long serialVersionUID = 1L;

	SchemaTreeModel(TreeNode<NodeDefinition> root) {
		super(root);
	}
	
	public static SchemaTreeModel createInstance(CollectSurvey survey) {
		return createInstance(survey, null);
	}
	
	public static SchemaTreeModel createInstance(CollectSurvey survey, ModelVersion version) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootDefns = schema.getRootEntityDefinitions();
		List<TreeNode<NodeDefinition>> treeNodes = NodeDefinitionTreeNode.fromList(rootDefns, version);
		TreeNode<NodeDefinition> root = new NodeDefinitionTreeNode(null, treeNodes);
		SchemaTreeModel result = new SchemaTreeModel(root);
		return result;
	}
	
	public static class NodeDefinitionTreeNode extends DefaultTreeNode<NodeDefinition> {
		
		private static final long serialVersionUID = 1L;
		
		public NodeDefinitionTreeNode(AttributeDefinition data) {
			super(data);
		}
		
		public NodeDefinitionTreeNode(EntityDefinition data, Collection<TreeNode<NodeDefinition>> children) {
			super(data, children);
		}
		
		public NodeDefinitionTreeNode(EntityDefinition data) {
			super(data, new ArrayList<TreeNode<NodeDefinition>>());
		}
		
		public static List<TreeNode<NodeDefinition>> fromList(List<? extends NodeDefinition> items,
				ModelVersion version) {
			List<TreeNode<NodeDefinition>> result = null;
			if ( items != null ) {
				result = new ArrayList<TreeNode<NodeDefinition>>();
				for (NodeDefinition item : items) {
					if ( version == null || version.isApplicable(item) ) {
						List<TreeNode<NodeDefinition>> childrenNodes = null;
						NodeDefinitionTreeNode node;
						if ( item instanceof EntityDefinition ) {
							List<NodeDefinition> childDefns = ((EntityDefinition) item).getChildDefinitions();
							childrenNodes = fromList(childDefns, version);
							node = new NodeDefinitionTreeNode((EntityDefinition) item, childrenNodes);
						} else {
							node = new NodeDefinitionTreeNode((AttributeDefinition) item);	
						}
						result.add(node);
					}
				}
			}
			return result;
		}
		
//		@Override
//		public void insert(TreeNode<NodeDefinition> child, int index) {
//			if ( isLeaf() ) {
//				addEmptyChildren();
//			}
//			super.insert(child, index);
//		}
//
//		protected void addEmptyChildren() {
//			NodeDefinitionTreeNode newThisNode = new NodeDefinitionTreeNode(this.getData(), new ArrayList<TreeNode<NodeDefinition>>());
//			NodeDefinitionTreeNode parent = (NodeDefinitionTreeNode) getParent();
//			parent.replace(this, newThisNode);
//		}
		
		public void replace(NodeDefinitionTreeNode oldNode, NodeDefinitionTreeNode newNode) {
			int index = this.getIndex(oldNode);
			this.remove(index);
			this.insert(newNode, index);
		}
		
	}
	
	public void deselect() {
		Collection<NodeDefinitionTreeNode> emptySelection = Collections.emptyList();
		setSelection(emptySelection);
	}

	public void removeSelectedNode() {
		int[] selectionPath = getSelectionPath();
		TreeNode<NodeDefinition> treeNode = getChild(selectionPath);
		TreeNode<NodeDefinition> parentTreeNode = treeNode.getParent();
		parentTreeNode.remove(treeNode);
	}
	
	public void appendNodeToSelected(NodeDefinition item) {
		NodeDefinitionTreeNode treeNode;
		if ( item instanceof EntityDefinition ) {
			treeNode = new NodeDefinitionTreeNode((EntityDefinition) item);
		} else {
			treeNode = new NodeDefinitionTreeNode((AttributeDefinition) item);
		}
		int[] selectionPath = getSelectionPath();
		if ( selectionPath == null || item.getParentDefinition() == null ) {
			TreeNode<NodeDefinition> root = getRoot();
			root.add(treeNode);
		} else {
			TreeNode<NodeDefinition> selectedTreeNode = getChild(selectionPath);
			selectedTreeNode.add(treeNode);
		}
		addOpenObject(treeNode.getParent());
		setSelection(Arrays.asList(treeNode));
	}
	
	public void moveSelectedNode(int toIndex) {
		int[] selectionPath = getSelectionPath();
		NodeDefinitionTreeNode treeNode = (NodeDefinitionTreeNode) getChild(selectionPath);
		TreeNode<NodeDefinition> parentTreeNode = treeNode.getParent();
		parentTreeNode.remove(treeNode);
		parentTreeNode.insert(treeNode, toIndex);
		List<NodeDefinitionTreeNode> selection = Arrays.asList(treeNode);
		setSelection(selection);
	}
	
}
