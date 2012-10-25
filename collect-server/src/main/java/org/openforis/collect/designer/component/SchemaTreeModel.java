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

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaTreeModel extends DefaultTreeModel<NodeDefinition> {
	
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
	
	public static SchemaTreeModel createInstance(CollectSurvey survey, ModelVersion version, boolean includeAttributes) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootDefns = schema.getRootEntityDefinitions();
		List<NodeDefinitionTreeNode> treeNodes = NodeDefinitionTreeNode.fromList(rootDefns, version, includeAttributes);
		NodeDefinitionTreeNode root = new NodeDefinitionTreeNode(null, treeNodes);
		SchemaTreeModel result = new SchemaTreeModel(root, version, includeAttributes);
		return result;
	}
	
	public void deselect() {
		Collection<NodeDefinitionTreeNode> emptySelection = Collections.emptyList();
		setSelection(emptySelection);
	}

	public void removeSelectedNode() {
		int[] selectionPath = getSelectionPath();
		NodeDefinitionTreeNode treeNode = (NodeDefinitionTreeNode) getChild(selectionPath);
		NodeDefinitionTreeNode parentTreeNode = (NodeDefinitionTreeNode) treeNode.getParent();
		parentTreeNode.remove(treeNode);
	}
	
	public void appendNodeToSelected(NodeDefinition nodeDefn) {
		int[] selectionPath = getSelectionPath();
		NodeDefinitionTreeNode parentNode;
		if ( selectionPath == null || nodeDefn.getParentDefinition() == null ) {
			parentNode = (NodeDefinitionTreeNode) getRoot();
		} else {
			parentNode = (NodeDefinitionTreeNode) getChild(selectionPath);
		}
		NodeDefinitionTreeNode nodeToSelect;
		if ( parentNode.isLeaf() ) {
			parentNode = recreateNode(parentNode);
			nodeToSelect = getNode(nodeDefn);
		} else {
			NodeDefinitionTreeNode newNode = NodeDefinitionTreeNode.createNode(nodeDefn, version, includeAttributes);
			parentNode.add(newNode);
			nodeToSelect = newNode;
		}
		addOpenObject(parentNode);
		setSelection(Arrays.asList(nodeToSelect));
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
	
	protected NodeDefinitionTreeNode getNode(NodeDefinition defn) {
		if ( defn != null ) {
			int[] path = getPath(defn);
			return (NodeDefinitionTreeNode) getChild(path);
		} else {
			return null;
		}
	}
	
	protected int[] getPath(NodeDefinition defn) {
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
	
	protected NodeDefinitionTreeNode recreateNode(NodeDefinitionTreeNode node) {
		NodeDefinitionTreeNode parent = (NodeDefinitionTreeNode) node.getParent();
		NodeDefinition data = node.getData();
		NodeDefinitionTreeNode newNode = NodeDefinitionTreeNode.createNode(data, version, includeAttributes);
		parent.replace(node, newNode);
		return newNode;
	}

	public void moveSelectedNode(int toIndex) {
		int[] selectionPath = getSelectionPath();
		NodeDefinitionTreeNode treeNode = (NodeDefinitionTreeNode) getChild(selectionPath);
		NodeDefinitionTreeNode parentTreeNode = (NodeDefinitionTreeNode) treeNode.getParent();
		parentTreeNode.remove(treeNode);
		parentTreeNode.insert(treeNode, toIndex);
		List<NodeDefinitionTreeNode> selection = Arrays.asList(treeNode);
		setSelection(selection);
	}

	static class NodeDefinitionTreeNode extends DefaultTreeNode<NodeDefinition> {
		
		private static final long serialVersionUID = 1L;
		
		NodeDefinitionTreeNode(AttributeDefinition data) {
			super(data);
		}
		
		NodeDefinitionTreeNode(EntityDefinition data) {
			super(data);
		}
		
		NodeDefinitionTreeNode(EntityDefinition data, Collection<NodeDefinitionTreeNode> children) {
			super(data, children);
		}
		
		public static NodeDefinitionTreeNode createNode(NodeDefinition item, ModelVersion version,
				boolean includeAttributes) {
			NodeDefinitionTreeNode node = null;
			if ( item instanceof EntityDefinition ) {
				List<NodeDefinition> childDefns = ((EntityDefinition) item).getChildDefinitions();
				List<NodeDefinitionTreeNode> childNodes = fromList(childDefns, version, includeAttributes);
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
		
		public static List<NodeDefinitionTreeNode> fromList(List<? extends NodeDefinition> items,
				ModelVersion version, boolean includeAttributes) {
			List<NodeDefinitionTreeNode> result = null;
			if ( items != null ) {
				result = new ArrayList<NodeDefinitionTreeNode>();
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
