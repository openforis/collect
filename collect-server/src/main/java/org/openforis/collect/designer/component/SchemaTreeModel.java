package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
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
		
		public NodeDefinitionTreeNode(NodeDefinition data) {
			this(data, null);
		}
		
		public NodeDefinitionTreeNode(NodeDefinition data, Collection<TreeNode<NodeDefinition>> children) {
			super(data, children);
		}
		
		public static List<TreeNode<NodeDefinition>> fromList(List<? extends NodeDefinition> items) {
			List<TreeNode<NodeDefinition>> result = null;
			if ( items != null ) {
				result = new ArrayList<TreeNode<NodeDefinition>>();
				for (NodeDefinition item : items) {
					List<TreeNode<NodeDefinition>> childrenNodes = null;
					if ( item instanceof EntityDefinition ) {
						List<NodeDefinition> childDefns = ((EntityDefinition) item).getChildDefinitions();
						childrenNodes = fromList(childDefns);
					}
					NodeDefinitionTreeNode node = new NodeDefinitionTreeNode(item, childrenNodes);
					result.add(node);
				}
			}
			return result;
		}
		
		@Override
		public List<TreeNode<NodeDefinition>> getChildren() {
			NodeDefinition nodeDefn = getData();
			if ( nodeDefn instanceof EntityDefinition ) {
				List<NodeDefinition> childDefinitions = ((EntityDefinition) nodeDefn).getChildDefinitions();
				List<TreeNode<NodeDefinition>> result = fromList(childDefinitions);
				return result;
			} else {
				return null;
			}
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
		NodeDefinitionTreeNode treeNode = new NodeDefinitionTreeNode(item);
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
