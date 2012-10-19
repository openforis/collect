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

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaTreeModel extends DefaultTreeModel<NodeDefinition> {
	
	private static final long serialVersionUID = 1L;
	
	private boolean includeAttributes;

	SchemaTreeModel(TreeNode<NodeDefinition> root, boolean includeAttributes) {
		super(root);
		this.includeAttributes = includeAttributes;
	}
	
	public void select(NodeDefinition defn) {
		if ( defn != null ) {
			NodeDefinitionTreeNode treeNode = getNode(defn);
			setSelection(Arrays.asList(treeNode));
		} else {
			List<NodeDefinitionTreeNode> emptyList = Collections.emptyList();
			setSelection(emptyList);
		}
	}
	
	public NodeDefinitionTreeNode getNode(NodeDefinition defn) {
		if ( defn != null ) {
			int[] path = getPath(defn);
			return (NodeDefinitionTreeNode) getChild(path);
		} else {
			return null;
		}
	}
	
	public int[] getPath(NodeDefinition defn) {
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

	private int[] toArray(List<Integer> temp) {
		int[] result = new int[temp.size()];
		for (int i = 0; i < temp.size(); i++) {
			int value = temp.get(i).intValue();
			result[i] = value;
		}
		return result;
	}
	
	public static SchemaTreeModel createInstance(CollectSurvey survey, boolean includeAttributes) {
		return createInstance(survey, null, includeAttributes);
	}
	
	public static SchemaTreeModel createInstance(CollectSurvey survey, ModelVersion version, boolean includeAttributes) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootDefns = schema.getRootEntityDefinitions();
		List<TreeNode<NodeDefinition>> treeNodes = NodeDefinitionTreeNode.fromList(rootDefns, version, includeAttributes);
		TreeNode<NodeDefinition> root = new NodeDefinitionTreeNode(null, treeNodes);
		SchemaTreeModel result = new SchemaTreeModel(root, includeAttributes);
		return result;
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

	static class NodeDefinitionTreeNode extends DefaultTreeNode<NodeDefinition> {
		
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
				ModelVersion version, boolean includeAttributes) {
			List<TreeNode<NodeDefinition>> result = null;
			if ( items != null ) {
				result = new ArrayList<TreeNode<NodeDefinition>>();
				for (NodeDefinition item : items) {
					if ( version == null || version.isApplicable(item) ) {
						NodeDefinitionTreeNode node = null;
						if ( item instanceof EntityDefinition ) {
							List<NodeDefinition> childDefns = ((EntityDefinition) item).getChildDefinitions();
							List<TreeNode<NodeDefinition>> childrenNodes = fromList(childDefns, version, includeAttributes);
							node = new NodeDefinitionTreeNode((EntityDefinition) item, childrenNodes);
						} else if ( includeAttributes ) {
							node = new NodeDefinitionTreeNode((AttributeDefinition) item);	
						}
						if ( node != null ) {
							result.add(node);
						}
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
	

}
