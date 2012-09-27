package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeNode;

public class SchemaTreeModel extends DefaultTreeModel<NodeDefinition> {
	
	private static final long serialVersionUID = 1L;

	SchemaTreeModel(TreeNode<NodeDefinition> root) {
		super(root);
	}
	
	public static SchemaTreeModel createInstance(CollectSurvey survey) {
		List<EntityDefinition> rootDefns = survey.getSchema().getRootEntityDefinitions();
		List<TreeNode<NodeDefinition>> treeNodes = NodeDefinitionTreeNode.fromList(rootDefns);
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
		
		public static List<TreeNode<NodeDefinition>> fromList(List<? extends NodeDefinition> items) {
			List<TreeNode<NodeDefinition>> result = null;
			if ( items != null ) {
				result = new ArrayList<TreeNode<NodeDefinition>>();
				for (NodeDefinition item : items) {
					List<TreeNode<NodeDefinition>> childrenNodes = null;
					NodeDefinitionTreeNode node;
					if ( item instanceof EntityDefinition ) {
						List<NodeDefinition> childDefns = ((EntityDefinition) item).getChildDefinitions();
						childrenNodes = fromList(childDefns);
						node = new NodeDefinitionTreeNode((EntityDefinition) item, childrenNodes);
					} else {
						node = new NodeDefinitionTreeNode((AttributeDefinition) item);	
					}
					result.add(node);
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
	
}
