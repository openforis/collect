package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.openforis.collect.designer.model.NodeType;
import org.openforis.collect.designer.viewmodel.SchemaVM;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyObject;
import org.zkoss.bind.BindUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.TreeNode;

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaTreeModel extends AbstractTreeModel<SchemaTreeModel.SchemaNodeData> {
	
	private static final long serialVersionUID = 1L;
	
	private ModelVersion version;
	
	private EntityDefinition rootEntity;
	private boolean includeAttributes;
	private String labelLanguage;

	SchemaTreeModel(AbstractNode<SchemaNodeData> root, EntityDefinition rootEntity, ModelVersion version, boolean includeAttributes, String labelLanguage) {
		super(root);
		this.rootEntity = rootEntity;
		this.includeAttributes = includeAttributes;
		this.version = version;
		this.labelLanguage = labelLanguage;
	}
	
	public static SchemaTreeModel createInstance(EntityDefinition rootEntity, ModelVersion version, boolean includeAttributes, String labelLanguage) {
		if ( rootEntity != null && (version == null || version.isApplicable(rootEntity)) ) {
			Collection<AbstractNode<SchemaNodeData>> firstLevelTreeNodes = new ArrayList<AbstractNode<SchemaNodeData>>();
			CollectSurvey survey = (CollectSurvey) rootEntity.getSurvey();
			UIOptions uiOptions = survey.getUIOptions();
			UITabSet tabSet = uiOptions.getAssignedRootTabSet(rootEntity);
			for (UITab tab : tabSet.getTabs()) {
				SchemaTreeNode node = SchemaTreeNode.createNode(tab, version, includeAttributes, false, labelLanguage);
				firstLevelTreeNodes.add(node);
			}
			SchemaTreeNode root = new SchemaTreeNode(null, firstLevelTreeNodes);
			SchemaTreeModel result = new SchemaTreeModel(root, rootEntity, version, includeAttributes, labelLanguage);
			return result;
		} else {
			return null;
		}
	}
	
	@Override
	protected AbstractNode<SchemaNodeData> createNode(SchemaNodeData data, boolean defineEmptyChildrenForLeaves) {
		AbstractNode<SchemaNodeData> result = SchemaTreeNode.createNode(data, version, includeAttributes, defineEmptyChildrenForLeaves, labelLanguage);
		return result;
	}
	
	public SchemaNodeData getNodeData(SurveyObject surveyObject) {
		int[] path = getNodePath(surveyObject);
		return getNodeData(path);
	}
	
	public EntityDefinition getNearestParentEntityDefinition(SurveyObject surveyObject) {
		SchemaTreeNode treeNode = getTreeNode(surveyObject);
		SchemaTreeNode parentNode = (SchemaTreeNode)  treeNode.getParent();
		while ( parentNode != null && parentNode.getData() != null ) {
			SurveyObject currentSurveyObject = parentNode.getData().getSurveyObject();
			if ( currentSurveyObject instanceof EntityDefinition ) {
				return (EntityDefinition) currentSurveyObject;
			}
			parentNode = (SchemaTreeNode) parentNode.getParent();
		}
		//if not found, return root entity
		return rootEntity;
	}

	private SchemaNodeData getNodeData(int[] path) {
		if ( path == null ) {
			return null;
		} else {
			SchemaTreeNode node = (SchemaTreeNode) getChild(path);
			SchemaNodeData data = node.getData();
			return data;
		}
	}
	
	protected int[] getNodePath(SurveyObject surveyObject) {
		TreeNode<SchemaNodeData> treeNode = getTreeNode(surveyObject);
		if ( treeNode == null ) {
			return null;
		} else {
			int[] result = super.getPath(treeNode);
			return result;
		}
	}
	
	public SchemaTreeNode getTreeNode(SurveyObject surveyObject) {
		TreeNode<SchemaNodeData> root = getRoot();
		Stack<SchemaTreeNode> stack = new Stack<SchemaTreeNode>();
		stack.push((SchemaTreeNode) root);
		while ( ! stack.isEmpty() ) {
			SchemaTreeNode treeNode = stack.pop();
			SchemaNodeData treeNodeData = treeNode.getData();
			if ( treeNodeData != null && treeNodeData.getSurveyObject() == surveyObject ) {
				return treeNode;
			}
			List<TreeNode<SchemaNodeData>> children = treeNode.getChildren();
			if ( children != null && children.size() > 0 ) {
				for (TreeNode<SchemaNodeData> child : children) {
					stack.push((SchemaTreeNode) child);
				}
			}
		}
		return null;
	}

	public SchemaNodeData select(SurveyObject surveyObject) {
		SchemaNodeData data = getNodeData(surveyObject);
		super.select(data);
		return data;
	}
	
	public void updateNodeLabel(SurveyObject surveyObject, String label) {
		SchemaNodeData data = getNodeData(surveyObject);
		data.setLabel(label);
		BindUtils.postNotifyChange(null, null, data, "label");
	}
	
	public void appendNodeToSelected(SurveyObject surveyObject) {
		appendNodeToSelected(surveyObject, false);
	}
	
	public void appendNodeToSelected(SurveyObject surveyObject, boolean detached) {
		AbstractNode<?> selectedNode = getSelectedNode();
		boolean root = selectedNode == null;
		String label;
		if ( detached ) {
			label = SchemaNodeData.getDetachedLabel(surveyObject, root);
		} else if ( surveyObject instanceof NodeDefinition ) {
			label = ((NodeDefinition) surveyObject).getName();
		} else {
			label = ((UITab) surveyObject).getLabel(labelLanguage);
		}
		SchemaNodeData data = new SchemaNodeData(surveyObject, label, root, detached);
		super.appendNodeToSelected(data);
	}
	
	public void setSelectedNodeLabel(String label) {
		AbstractNode<SchemaNodeData> selectedNode = getSelectedNode();
		if ( selectedNode != null ) {
			SchemaNodeData data = selectedNode.getData();
			data.setLabel(label);
			BindUtils.postNotifyChange(null, null, data, "label");
		}
	}
	
	public void markSelectedNodeAsDetached() {
		AbstractNode<SchemaNodeData> selectedNode = getSelectedNode();
		SimpleNodeData data = selectedNode.getData();
		data.setDetached(true);
	}

	public List<SurveyObject> getSiblingsAndSelf(SurveyObject obj, boolean sameType) {
		List<SurveyObject> result = new ArrayList<SurveyObject>();
		TreeNode<SchemaNodeData> treeNode = getTreeNode(obj);
		SchemaTreeNode parent = (SchemaTreeNode) treeNode.getParent();
		List<TreeNode<SchemaNodeData>> children = parent.getChildren();
		for (TreeNode<SchemaNodeData> child : children) {
			SurveyObject surveyObject = child.getData().getSurveyObject();
			if ( sameType && (
					(obj instanceof UITab && surveyObject instanceof UITab)
					||
					(obj instanceof NodeDefinition && surveyObject instanceof NodeDefinition)
				) ) {
				result.add(surveyObject);
			}
		}
		return result;
	}

	protected SchemaTreeNode recreateNode(SchemaTreeNode node, boolean defineEmptyChildrenForLeaves) {
		SchemaTreeNode parent = (SchemaTreeNode) node.getParent();
		SchemaNodeData data = node.getData();
		SchemaTreeNode newNode = (SchemaTreeNode) SchemaTreeNode.createNode(data, version, includeAttributes, defineEmptyChildrenForLeaves, labelLanguage);
		parent.replace(node, newNode);
		return newNode;
	}

	public static class SchemaTreeNode extends AbstractNode<SchemaNodeData> {
		
		private static final long serialVersionUID = 1L;
		
		SchemaTreeNode(SchemaNodeData data) {
			super(data);
		}
		
		SchemaTreeNode(SchemaNodeData data, Collection<AbstractNode<SchemaNodeData>> children) {
			super(data, children);
		}
		
		public void markAsDetached() {
			SimpleNodeData data = getData();
			data.setDetached(true);
//			data.setLabel(SchemaTreeNodeData.getDetachedLabel(nodeDefinition, root));
		}
		
		@Override
		public String getIcon() {
			SchemaNodeData data = getData();
			return SchemaVM.getIcon(data);
		}
		
		public int getIndexInModel() {
			int result;
			SchemaNodeData data = getData();
			SurveyObject surveyObject = data.getSurveyObject();
			if ( surveyObject instanceof NodeDefinition ) {
				EntityDefinition parentEntity = ((NodeDefinition) surveyObject).getParentEntityDefinition();
				result = parentEntity.getChildDefinitionIndex((NodeDefinition) surveyObject);
			} else {
				result = ((UITab) surveyObject).getIndex();
			}
			return result;
		}
		
		public static AbstractNode<SchemaNodeData> createNode(SchemaNodeData data, ModelVersion version,
				boolean includeAttributes, boolean defineEmptyChildrenForLeaves, String labelLanguage) {
			SurveyObject surveyObject = data.getSurveyObject();
			SchemaTreeNode node = null;
			List<AbstractNode<SchemaNodeData>> childNodes = new ArrayList<AbstractNode<SchemaNodeData>>();
			if ( surveyObject instanceof NodeDefinition ) {
				NodeDefinition nodeDefn = (NodeDefinition) surveyObject;
				if ( nodeDefn instanceof EntityDefinition ) {
					List<AbstractNode<SchemaNodeData>> entityChildrenNodes = createChildNodes(nodeDefn, version, includeAttributes,
							labelLanguage);
					childNodes.addAll(entityChildrenNodes);
				} else if ( nodeDefn instanceof AttributeDefinition && ! includeAttributes ) {
					return null;
				}
			} else if ( surveyObject instanceof UITab ) {
				UITab tab = (UITab) surveyObject;
				
				List<SchemaTreeNode> childTabNodes = createChildNodes(tab, version, includeAttributes,
						labelLanguage);
				childNodes.addAll(childTabNodes);
			}
			//create result
			if ( childNodes == null || childNodes.isEmpty() ) {
				if ( defineEmptyChildrenForLeaves ) {
					node = new SchemaTreeNode(data, Collections.<AbstractNode<SchemaNodeData>>emptyList());
				} else {
					node = new SchemaTreeNode(data);
				}
			} else {
				node = new SchemaTreeNode(data, (List<AbstractNode<SchemaNodeData>>) childNodes);
			}
			return node;
		}

		private static List<AbstractNode<SchemaNodeData>> createChildNodes(NodeDefinition nodeDefn,
				ModelVersion version, boolean includeAttributes,
				String labelLanguage) {
			List<AbstractNode<SchemaNodeData>> childNodes = new ArrayList<AbstractNode<SchemaNodeData>>();
			CollectSurvey survey = (CollectSurvey) nodeDefn.getSurvey();
			UIOptions uiOptions = survey.getUIOptions();
			UITab assignedTab = uiOptions.getAssignedTab(nodeDefn);
			
			EntityDefinition entityDefn = (EntityDefinition) nodeDefn;
			List<NodeDefinition> childDefns = entityDefn.getChildDefinitions();
			Collection<? extends AbstractNode<SchemaNodeData>> schemaTreeNodes = fromSchemaNodeList(assignedTab, childDefns, version, 
					includeAttributes, labelLanguage);
			childNodes.addAll(schemaTreeNodes);
			
			//include tabs
			List<UITab> tabs = uiOptions.getTabsAssignableToChildren(entityDefn);
			Collection<? extends AbstractNode<SchemaNodeData>> tabNodes = fromTabsList(tabs, version, includeAttributes, labelLanguage);
			childNodes.addAll(tabNodes);
			return childNodes;
		}

		private static List<SchemaTreeNode> createChildNodes(UITab tab, ModelVersion version,
				boolean includeAttributes, String labelLanguage) {
			List<SchemaTreeNode> result = new ArrayList<SchemaTreeNode>();
			//add schema node definition tree nodes
			UIOptions uiOptions = tab.getUIOptions();
			List<NodeDefinition> childDefns = uiOptions.getNodesPerTab(tab, false);
			List<SchemaTreeNode> childSchemaNodes = SchemaTreeNode.fromSchemaNodeList(tab, childDefns, version, includeAttributes, labelLanguage);
			result.addAll(childSchemaNodes);
			
			//add children unassigned tab tree nodes
//			List<UITab> unassignedTabs = new ArrayList<UITab>();
//			List<UITab> tabs = tab.getTabs();
//			for (UITab childTab : tabs) {
//				UITabSet rootTabSet = childTab.getRootTabSet();
//				EntityDefinition parentEntity = uiOptions.getRootEntityDefinition(rootTabSet);
//				boolean unassigned = uiOptions.isUnassigned(childTab, parentEntity);
//				if ( unassigned ) {
//					unassignedTabs.add(childTab);
//				}
//			}
//			List<SchemaTreeNode> unassignedTabNodes = fromTabsList(unassignedTabs, version, includeAttributes, labelLanguage);
//			result.addAll(unassignedTabNodes);
			return result;
		}
		
		public static SchemaTreeNode createNode(NodeDefinition item, ModelVersion version,
				boolean includeAttributes, boolean defineEmptyChildrenForLeaves, String labelLangugage) {
			SchemaNodeData data = new SchemaNodeData(item, item.getName(), false, false);
			return (SchemaTreeNode) createNode(data, version, includeAttributes, defineEmptyChildrenForLeaves, labelLangugage);
		}
		
		public static SchemaTreeNode createNode(UITab tab, ModelVersion version,
				boolean includeAttributes, boolean defineEmptyChildrenForLeaves, String labelLanguage) {
			SchemaNodeData data = new SchemaNodeData(tab, tab.getLabel(labelLanguage), false, false);
			return (SchemaTreeNode) createNode(data, version, includeAttributes, defineEmptyChildrenForLeaves, labelLanguage);
		}
		
		protected static List<SchemaTreeNode> fromSchemaNodeList(UITab parentTab, 
				List<? extends NodeDefinition> items,
				ModelVersion version, boolean includeAttributes, String labelLanguage) {
			List<SchemaTreeNode> result = null;
			if ( items != null ) {
				result = new ArrayList<SchemaTreeNode>();
				for (NodeDefinition item : items) {
					CollectSurvey survey = (CollectSurvey) item.getSurvey();
					UIOptions uiOptions = survey.getUIOptions();
					UITab assignedTab = uiOptions.getAssignedTab(item);
					if ( assignedTab == parentTab && ( version == null || version.isApplicable(item) ) ) {
						SchemaTreeNode node = createNode(item, version, includeAttributes, false, labelLanguage);
						if ( node != null ) {
							result.add(node);
						}
					}
				}
			}
			return result;
		}

		protected static List<SchemaTreeNode> fromTabsList(List<UITab> tabs, ModelVersion version, 
				boolean includeAttributes, String labelLanguage) {
			List<SchemaTreeNode> result = null;
			if ( tabs != null ) {
				result = new ArrayList<SchemaTreeNode>();
				for (UITab tab : tabs) {
					SchemaTreeNode node = createNode(tab, version, includeAttributes, false, labelLanguage);
					if ( node != null ) {
						result.add(node);
					}
				}
			}
			return result;
		}
	}
	
	public static class SchemaNodeData extends AbstractTreeModel.SimpleNodeData {
		
		private SurveyObject surveyObject;
		
		protected SchemaNodeData(SurveyObject surveyObject, String label, boolean root, boolean detached) {
			super(label, root, detached);
			this.surveyObject = surveyObject;
		}

		protected static String getDetachedLabel(SurveyObject surveyObject, boolean root) {
			String result;
			if ( surveyObject instanceof NodeDefinition ) {
				String nodeTypeLabel = NodeType.getHeaderLabel((NodeDefinition) surveyObject, root, true);
				Object[] args = new String[]{nodeTypeLabel};
				result = Labels.getLabel("survey.schema.tree.new_node_label", args);
			} else {
				//TODO
				result = "NEW TAB";
			}
			return result;
		}
		
		public SurveyObject getSurveyObject() {
			return surveyObject;
		}

	}

}
