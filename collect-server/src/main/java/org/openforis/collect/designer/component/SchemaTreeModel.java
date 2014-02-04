package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.openforis.collect.designer.model.NodeType;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
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
public class SchemaTreeModel extends AbstractTreeModel<AbstractTreeModel.NodeData> {
	
	private static final long serialVersionUID = 1L;
	
	private ModelVersion version;
	
	private boolean singleRootEntity;
	private boolean includeAttributes;

	SchemaTreeModel(AbstractTreeModel.AbstractNode<NodeData> root, ModelVersion version, boolean singleRootEntity, boolean includeAttributes) {
		super(root);
		this.singleRootEntity = singleRootEntity;
		this.includeAttributes = includeAttributes;
		this.version = version;
	}
	
	public static SchemaTreeModel createInstance(EntityDefinition rootEntity, ModelVersion version, boolean includeAttributes) {
		if ( rootEntity != null && (version == null || version.isApplicable(rootEntity)) ) {
			List<TabTreeNode> firstLevelTreeNodes = new ArrayList<TabTreeNode>();
			CollectSurvey survey = (CollectSurvey) rootEntity.getSurvey();
			UIOptions uiOptions = survey.getUIOptions();
			UITabSet tabSet = uiOptions.getAssignedRootTabSet(rootEntity);
			for (UITab tab : tabSet.getTabs()) {
				TabTreeNode node = SchemaTreeNode.createNode(tab, version, includeAttributes, false);
				firstLevelTreeNodes.add(node);
			}
			TabTreeNode root = new TabTreeNode(null, firstLevelTreeNodes);
			SchemaTreeModel result = new SchemaTreeModel(root, version, true, includeAttributes);
			result.openAllItems();
			return result;
		} else {
			return null;
		}
	}
	
	@Override
	protected AbstractNode<NodeData> createNode(NodeData data, boolean defineEmptyChildrenForLeaves) {
		return createNodeInternal(data, defineEmptyChildrenForLeaves);
	}
	
	@SuppressWarnings("unchecked")
	private <T extends AbstractNode<D>, D extends NodeData> T createNodeInternal(D data, boolean defineEmptyChildrenForLeaves) {
		T result;
		if ( data instanceof SchemaNodeData ) {
			result = (T) SchemaTreeNode.createNode((SchemaNodeData) data, version, includeAttributes, defineEmptyChildrenForLeaves);
		} else {
			result = (T) SchemaTreeNode.createNode((TabNodeData) data, version, includeAttributes, defineEmptyChildrenForLeaves);
		}
		return result;
	}
	
	public SchemaNodeData getNodeData(NodeDefinition nodeDefn) {
		int[] path = getNodePath(nodeDefn);
		if ( path == null ) {
			return null;
		} else {
			TreeNode<NodeData> node = getChild(path);
			SchemaNodeData data = (SchemaNodeData) node.getData();
			return data;
		}
	}
	
	protected int[] getNodePath(NodeDefinition nodeDefn) {
		@SuppressWarnings("rawtypes")
		TreeNode treeNode = getTreeNode(nodeDefn);
		if ( treeNode == null ) {
			return null;
		} else {
			@SuppressWarnings("unchecked")
			int[] result = super.getPath(treeNode);
			return result;
		}
	}
	
	protected SchemaTreeNode getTreeNode(NodeDefinition nodeDefn) {
		TreeNode<NodeData> root = getRoot();
		Stack<TreeNode<?>> treeNodesStack = new Stack<TreeNode<?>>();
		treeNodesStack.push(root);
		while ( ! treeNodesStack.isEmpty() ) {
			TreeNode<?> treeNode = treeNodesStack.pop();
			NodeData treeNodeData = (NodeData) treeNode.getData();
			if ( treeNodeData != null && treeNodeData instanceof SchemaNodeData && 
					((SchemaNodeData) treeNodeData).getNodeDefinition().equals(nodeDefn) ) {
				return (SchemaTreeNode) treeNode;
			}
			List<?> children = treeNode.getChildren();
			if ( children != null && children.size() > 0 ) {
				for (Object child : children) {
					treeNodesStack.push((TreeNode<?>) child);
				}
			}
		}
		return null;
	}
	
	public void select(NodeDefinition nodeDefn) {
		SchemaNodeData data = getNodeData(nodeDefn);
		super.select(data);
	}
	
	public void appendNodeToSelected(NodeDefinition nodeDefn) {
		appendNodeToSelected(nodeDefn, false);
	}
	
	public void appendNodeToSelected(NodeDefinition nodeDefn, boolean detached) {
		AbstractNode<?> selectedNode = getSelectedNode();
		boolean root = selectedNode == null;
		String label = detached ? SchemaNodeData.getDetachedLabel(nodeDefn, root): nodeDefn.getName();
		SchemaNodeData data = new SchemaNodeData(nodeDefn, label, root, detached);
		super.appendNodeToSelected(data);
	}
	
	public void setSelectedNodeLabel(String label) {
		AbstractNode<NodeData> selectedNode = getSelectedNode();
		if ( selectedNode != null ) {
			NodeData data = selectedNode.getData();
			data.setLabel(label);
			BindUtils.postNotifyChange(null, null, data, "label");
		}
	}
	
	public void markSelectedNodeAsDetached() {
		AbstractNode<NodeData> selectedNode = getSelectedNode();
		NodeData data = selectedNode.getData();
		data.setDetached(true);
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

	protected TreeNode<NodeData> recreateNode(TreeNode<NodeData> node, boolean defineEmptyChildrenForLeaves) {
		AbstractNode<NodeData> parent = (AbstractNode<NodeData>) node.getParent();
		NodeData data = (NodeData) node.getData();
		TreeNode<NodeData> newNode; 
		if ( data instanceof SchemaNodeData ) {
			NodeDefinition nodeDefn = ((SchemaNodeData) data).getNodeDefinition();
			newNode = SchemaTreeNode.createNode(nodeDefn, version, includeAttributes, defineEmptyChildrenForLeaves);
		} else {
			UITab tab = ((TabNodeData) data).getTab();
			newNode = SchemaTreeNode.createNode(tab, version, includeAttributes, defineEmptyChildrenForLeaves);
		}
		parent.replace(node, newNode);
		return newNode;
	}

	static class TabTreeNode extends AbstractNode<NodeData> {

		private static final long serialVersionUID = 1L;
		
		TabTreeNode(TabNodeData data, Collection<? extends AbstractNode<NodeData>> children) {
			super(data, children);
		}

		TabTreeNode(TabNodeData data) {
			super(data);
		}
		
	}
	
	static class SchemaTreeNode extends AbstractNode<NodeData> {
		
		private static final long serialVersionUID = 1L;
		
		SchemaTreeNode(SchemaNodeData data) {
			super(data);
		}
		
		SchemaTreeNode(SchemaNodeData data, Collection<SchemaTreeNode> children) {
			super(data, children);
		}
		
		public void markAsDetached() {
			NodeData data = getData();
			data.setDetached(true);
//			data.setLabel(SchemaTreeNodeData.getDetachedLabel(nodeDefinition, root));
		}
		
		public static SchemaTreeNode createNode(SchemaNodeData data, ModelVersion version,
				boolean includeAttributes, boolean defineEmptyChildrenForLeaves) {
			SchemaTreeNode node = null;
			NodeDefinition nodeDefn = data.getNodeDefinition();
			if ( nodeDefn instanceof EntityDefinition ) {
				List<NodeDefinition> childDefns = ((EntityDefinition) nodeDefn).getChildDefinitions();
				List<SchemaTreeNode> childNodes = fromSchemaNodeList(childDefns, version, includeAttributes);
				if ( childNodes == null || childNodes.isEmpty() ) {
					if ( defineEmptyChildrenForLeaves ) {
						node = new SchemaTreeNode(data, Collections.<SchemaTreeNode>emptyList());
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
		
		public static TabTreeNode createNode(TabNodeData data, ModelVersion version,
				boolean includeAttributes, boolean defineEmptyChildrenForLeaves) {
			Collection<AbstractNode<NodeData>> childNodes = new ArrayList<AbstractNode<NodeData>>();
			
			UITab tab = data.getTab();
			UIOptions uiOptions = tab.getUIOptions();
			
			List<UITab> tabs = tab.getTabs();
			List<TabTreeNode> childTabNodes = fromTabsList(tabs, version, includeAttributes);
			List<NodeDefinition> childDefns = uiOptions.getNodesPerTab(tab, false);
			List<SchemaTreeNode> childSchemaNodes = fromSchemaNodeList(childDefns, version, includeAttributes);
			childNodes.addAll(childSchemaNodes);
			childNodes.addAll(childTabNodes);
			TabTreeNode node = new TabTreeNode(data, childNodes);
			return node;
		}
		
		public static SchemaTreeNode createNode(NodeDefinition item, ModelVersion version,
				boolean includeAttributes, boolean defineEmptyChildrenForLeaves) {
			SchemaNodeData data = new SchemaNodeData(item, item.getName(), false, false);
			return createNode(data, version, includeAttributes, defineEmptyChildrenForLeaves);
		}
		
		public static TabTreeNode createNode(UITab tab, ModelVersion version,
				boolean includeAttributes, boolean defineEmptyChildrenForLeaves) {
			TabNodeData data = new TabNodeData(tab, tab.getName(), false, false);
			return createNode(data, version, includeAttributes, defineEmptyChildrenForLeaves);
		}
		
		protected static List<SchemaTreeNode> fromSchemaNodeList(List<? extends NodeDefinition> items,
				ModelVersion version, boolean includeAttributes) {
			List<SchemaTreeNode> result = null;
			if ( items != null ) {
				result = new ArrayList<SchemaTreeNode>();
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

		protected static List<TabTreeNode> fromTabsList(List<UITab> tabs, ModelVersion version, boolean includeAttributes) {
			List<TabTreeNode> result = null;
			if ( tabs != null ) {
				result = new ArrayList<TabTreeNode>();
				for (UITab tab : tabs) {
					TabTreeNode node = createNode(tab, version, includeAttributes, false);
					if ( node != null ) {
						result.add(node);
					}
				}
			}
			return result;
		}

	}

	public static class SchemaNodeData extends NodeData {
		
		private NodeDefinition nodeDefinition;
		
		protected SchemaNodeData(NodeDefinition nodeDefinition, String label, boolean root, boolean detached) {
			super(label, root, detached);
			this.nodeDefinition = nodeDefinition;
		}

		protected static String getDetachedLabel(NodeDefinition nodeDefinition, boolean root) {
			String nodeTypeLabel = NodeType.getHeaderLabel(nodeDefinition, root, true);
			Object[] args = new String[]{nodeTypeLabel};
			String result = Labels.getLabel("survey.schema.tree.new_node_label", args);
			return result;
		}
		
		public NodeDefinition getNodeDefinition() {
			return nodeDefinition;
		}

	}
	
	public static class TabNodeData extends NodeData {
		
		private UITab tab;

		public TabNodeData(UITab tab, String label, boolean root, boolean detached) {
			super(label, root, detached);
			this.tab = tab;
		}
		
		public UITab getTab() {
			return tab;
		}
		
	}

}
