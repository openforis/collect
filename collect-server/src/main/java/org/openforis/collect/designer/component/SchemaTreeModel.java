package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.openforis.collect.designer.model.NodeType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.BindUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.TreeNode;

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaTreeModel extends AbstractTreeModel<SchemaTreeModel.SchemaTreeNodeData> {
	
	private static final long serialVersionUID = 1L;
	
	private ModelVersion version;
	
	private boolean singleRootEntity;
	private boolean includeAttributes;

	SchemaTreeModel(SchemaTreeNode root, ModelVersion version, boolean singleRootEntity, boolean includeAttributes) {
		super(root);
		this.singleRootEntity = singleRootEntity;
		this.includeAttributes = includeAttributes;
		this.version = version;
	}
	
	public static SchemaTreeModel createInstance(CollectSurvey survey, boolean includeAttributes) {
		return createInstance(survey, (ModelVersion) null, includeAttributes);
	}
	
	@SuppressWarnings("unchecked")
	public static SchemaTreeModel createInstance(EntityDefinition rootEntity, ModelVersion version, boolean includeRootEntity, boolean includeAttributes) {
		if ( rootEntity != null && (version == null || version.isApplicable(rootEntity)) ) {
			List<AbstractTreeNode<SchemaTreeNodeData>> firstLevelTreeNodes;
			if ( includeRootEntity ) {
				AbstractTreeNode<SchemaTreeNodeData> rootEntityNode = SchemaTreeNode.createNode(rootEntity, version, includeAttributes, false);
				firstLevelTreeNodes = Arrays.asList(rootEntityNode);
			} else {
				firstLevelTreeNodes = SchemaTreeNode.fromList(rootEntity.getChildDefinitions(), version, includeAttributes);
			}
			SchemaTreeNode root = new SchemaTreeNode(null, firstLevelTreeNodes);
			SchemaTreeModel result = new SchemaTreeModel(root, version, true, includeAttributes);
			result.openAllItems();
			return result;
		} else {
			return null;
		}
	}
	
	public static SchemaTreeModel createInstance(CollectSurvey survey, ModelVersion version, boolean includeAttributes) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootDefns = schema.getRootEntityDefinitions();
		List<AbstractTreeNode<SchemaTreeNodeData>> treeNodes = SchemaTreeNode.fromList(rootDefns, version, includeAttributes);
		SchemaTreeNode root = new SchemaTreeNode(null, treeNodes);
		SchemaTreeModel result = new SchemaTreeModel(root, version, false, includeAttributes);
		return result;
	}
	
	@Override
	protected AbstractTreeNode<SchemaTreeNodeData> createNode(SchemaTreeNodeData data, boolean defineEmptyChildrenForLeaves) {
		SchemaTreeNode result = SchemaTreeNode.createNode(data, version, includeAttributes, defineEmptyChildrenForLeaves);
		return result;
	}
	
	public SchemaTreeNodeData getNodeData(NodeDefinition nodeDefn) {
		int[] path = getNodePath(nodeDefn);
		if ( path == null ) {
			return null;
		} else {
			TreeNode<SchemaTreeNodeData> node = getChild(path);
			SchemaTreeNodeData data = node.getData();
			return data;
		}
	}
	
	protected int[] getNodePath(NodeDefinition nodeDefn) {
		TreeNode<SchemaTreeNodeData> treeNode = getTreeNode(nodeDefn);
		if ( treeNode == null ) {
			return null;
		} else {
			int[] result = getPath(treeNode);
			return result;
		}
	}
	
	protected TreeNode<SchemaTreeNodeData> getTreeNode(NodeDefinition nodeDefn) {
		TreeNode<SchemaTreeNodeData> root = getRoot();
		Stack<TreeNode<SchemaTreeNodeData>> treeNodesStack = new Stack<TreeNode<SchemaTreeNodeData>>();
		treeNodesStack.push(root);
		while ( ! treeNodesStack.isEmpty() ) {
			TreeNode<SchemaTreeNodeData> treeNode = treeNodesStack.pop();
			SchemaTreeNodeData treeNodeData = treeNode.getData();
			if ( treeNodeData != null && treeNodeData.getNodeDefinition().equals(nodeDefn) ) {
				return treeNode;
			}
			List<TreeNode<SchemaTreeNodeData>> children = treeNode.getChildren();
			if ( children != null && children.size() > 0 ) {
				treeNodesStack.addAll(children);
			}
		}
		return null;
	}
	
	public void select(NodeDefinition nodeDefn) {
		SchemaTreeNodeData data = getNodeData(nodeDefn);
		super.select(data);
	}
	
	public void appendNodeToSelected(NodeDefinition nodeDefn) {
		appendNodeToSelected(nodeDefn, false);
	}
	
	public void appendNodeToSelected(NodeDefinition nodeDefn, boolean detached) {
		AbstractTreeNode<?> selectedNode = getSelectedNode();
		boolean root = selectedNode == null;
		SchemaTreeNodeData data = new SchemaTreeNodeData(nodeDefn, root, detached);
		super.appendNodeToSelected(data);
	}
	
	public void setSelectedNodeName(String name) {
		AbstractTreeNode<SchemaTreeNodeData> selectedNode = getSelectedNode();
		if ( selectedNode != null ) {
			SchemaTreeNodeData data = selectedNode.getData();
			data.setName(name);
			BindUtils.postNotifyChange(null, null, data, "name");
		}
	}
	
	public void markSelectedNodeAsDetached() {
		SchemaTreeNode selectedNode = (SchemaTreeNode) getSelectedNode();
		selectedNode.markAsDetached();
	}

	protected int getIndexInTree(EntityDefinition parent, NodeDefinition node) {
		List<NodeDefinition> siblings;
		if ( parent == null ) {
			if ( singleRootEntity ) {
				return 0;
			} else {
				Schema schema = node.getSchema();
				siblings = new ArrayList<NodeDefinition>();
				List<EntityDefinition> rootEntities = schema.getRootEntityDefinitions();
				for (EntityDefinition rootEntity : rootEntities) {
					if ( version == null || version.isApplicable(rootEntity) ) {
						siblings.add(rootEntity);
					}
				}
			}
		} else {
			siblings = getFilteredChildren(parent);
		}
		return siblings.indexOf(node);
	}
	
	protected List<NodeDefinition> getFilteredChildren(EntityDefinition parent) {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		List<NodeDefinition> allSiblings = parent.getChildDefinitions();
		for (NodeDefinition childDefn : allSiblings) {
			if ( isInVersion(childDefn) && (includeAttributes || childDefn instanceof EntityDefinition) ) {
				result.add(childDefn);
			}
		}
		return result;
	}

	protected boolean isInVersion(NodeDefinition childDefn) {
		return version == null || version.isApplicable(childDefn);
	}

	protected SchemaTreeNode recreateNode(SchemaTreeNode node, boolean defineEmptyChildrenForLeaves) {
		SchemaTreeNode parent = (SchemaTreeNode) node.getParent();
		SchemaTreeNodeData data = node.getData();
		NodeDefinition nodeDefn = data.getNodeDefinition();
		SchemaTreeNode newNode = SchemaTreeNode.createNode(nodeDefn, version, includeAttributes, defineEmptyChildrenForLeaves);
		parent.replace(node, newNode);
		return newNode;
	}

	static class SchemaTreeNode extends AbstractTreeNode<SchemaTreeNodeData> {
		
		private static final long serialVersionUID = 1L;
		
		SchemaTreeNode(SchemaTreeNodeData data) {
			super(data);
		}
		
		SchemaTreeNode(SchemaTreeNodeData data, Collection<AbstractTreeNode<SchemaTreeNodeData>> children) {
			super(data, children);
		}
		
		public void markAsDetached() {
			TreeNode<SchemaTreeNodeData> parent = getParent();
			TreeNode<SchemaTreeNodeData> root = getModel().getRoot();
			SchemaTreeNodeData data = getData();
			boolean rootEntity = parent == root;
			data.markAsDetached(rootEntity);
		}
		
		public static SchemaTreeNode createNode(SchemaTreeNodeData data, ModelVersion version,
				boolean includeAttributes, boolean defineEmptyChildrenForLeaves) {
			SchemaTreeNode node = null;
			NodeDefinition nodeDefn = data.getNodeDefinition();
			if ( nodeDefn instanceof EntityDefinition ) {
				List<NodeDefinition> childDefns = ((EntityDefinition) nodeDefn).getChildDefinitions();
				List<AbstractTreeNode<SchemaTreeNodeData>> childNodes = fromList(childDefns, version, includeAttributes);
				if ( childNodes == null || childNodes.isEmpty() ) {
					if ( defineEmptyChildrenForLeaves ) {
						List<AbstractTreeNode<SchemaTreeNodeData>> emptyChildrenList = Collections.emptyList();
						node = new SchemaTreeNode(data, emptyChildrenList);
					} else {
						node = new SchemaTreeNode(data);
					}
				} else {
					node = new SchemaTreeNode(data, childNodes);
				}
			} else if ( includeAttributes ) {
				node = new SchemaTreeNode(data);	
			}
			return node;
			
		}
		
		public static SchemaTreeNode createNode(NodeDefinition item, ModelVersion version,
				boolean includeAttributes, boolean defineEmptyChildrenForLeaves) {
			SchemaTreeNodeData data = new SchemaTreeNodeData(item);
			return createNode(data, version, includeAttributes, defineEmptyChildrenForLeaves);
		}
		
		public static List<AbstractTreeNode<SchemaTreeNodeData>> fromList(List<? extends NodeDefinition> items,
				ModelVersion version, boolean includeAttributes) {
			List<AbstractTreeNode<SchemaTreeNodeData>> result = null;
			if ( items != null ) {
				result = new ArrayList<AbstractTreeNode<SchemaTreeNodeData>>();
				for (NodeDefinition item : items) {
					if ( version == null || version.isApplicable(item) ) {
						SchemaTreeNode node = createNode(item, version, includeAttributes, false);
						if ( node != null ) {
							result.add(node);
						}
					}
				}
			}
			return result;
		}

	}

	public static class SchemaTreeNodeData {
		
		private boolean detached;
		private String name;
		private NodeDefinition nodeDefinition;
		
		protected SchemaTreeNodeData(NodeDefinition nodeDefinition) {
			super();
			this.nodeDefinition = nodeDefinition;
			this.name = nodeDefinition == null ? null: nodeDefinition.getName();
		}
		
		protected SchemaTreeNodeData(NodeDefinition nodeDefinition, boolean root, boolean detached) {
			this(nodeDefinition);
			this.detached = detached;
			if ( detached ) {
				this.name = getDetachedLabel(nodeDefinition, root);
			}
		}

		protected String getDetachedLabel(NodeDefinition nodeDefn, boolean root) {
			String nodeTypeLabel = NodeType.getHeaderLabel(nodeDefn, root, true);
			Object[] args = new String[]{nodeTypeLabel};
			String result = Labels.getLabel("survey.schema.tree.new_node_label", args);
			return result;
		}

		public void markAsDetached(boolean root) {
			if ( name == null ) {
				name = getDetachedLabel(nodeDefinition, root);
			}
		}
		
		public boolean isDetached() {
			return detached;
		}
		
		public void setDetached(boolean detached) {
			this.detached = detached;
		}
		
		public NodeDefinition getNodeDefinition() {
			return nodeDefinition;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}

}
