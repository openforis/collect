/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveySchemaEditVM extends SurveyEditVM {

	private static final String NODE_TYPE_ENTITY = "entity";
	private static final String NODE_TYPE_ATTRIBUTE = "attribute";

	private DefaultTreeModel<NodeDefinition> treeModel;
	
	private NodeDefinition selectedNode;
	
	private Map<String, Object> tempNode = new HashMap<String, Object>();
	
	private String nodeType;
	
	private Integer attributeTypeIndex;
	
	private boolean rootEntityCreation;
	
	private boolean newNode;
	
	private enum AttributeTypes {
		BOOLEAN, CODE, COORDINATE, DATE, FILE, NUMBER, RANGE, TAXON, TEXT, TIME
	}
	
	@NotifyChange({"tempNode","newNode","rootEntityCreation"})
	@Command
	public void nodeSelected(@BindingParam("node") Treeitem node) {
		if ( node != null ) {
			TreeNode<NodeDefinition> treeNode = node.getValue();
			selectedNode = treeNode.getData();
		} else {
			selectedNode = null;
		}
		initTempNode(selectedNode);
	}
	
	@NotifyChange({"tempNode","newNode","rootEntityCreation","nodeType"})
	@Command
	public void addRootEntity() {
		newNode = true;
		rootEntityCreation = true;
		nodeType = NODE_TYPE_ENTITY;
		selectedNode = null;
		tempNode = new HashMap<String, Object>();
		treeModel.setSelection(null);
	}
	
	@NotifyChange({"tempNode","newNode","rootEntityCreation","nodeType"})
	@Command
	public void addNode() throws Exception {
		if ( selectedNode != null && selectedNode instanceof EntityDefinition ) {
			tempNode = new HashMap<String, Object>();
			rootEntityCreation = false;
			newNode = true;
			nodeType = null;
			tempNode = new HashMap<String, Object>();
		} else {
			throw new Exception("Cannot add a child to an Attribute Definition");
		}
	}
	
	@NotifyChange({"selectedNode","tempNode","newNode","rootEntityCreation"})
	@Command
	public void saveNode() {
		EntityDefinition editedNode;
		if ( newNode && NODE_TYPE_ENTITY.equals(nodeType) ) {
			editedNode = new EntityDefinition();
		} else {
			//TODO
			editedNode = null;
		}
		copyCommonTempNodeProperties(editedNode);
		//TODO copy specific properties
		
		if ( newNode ) {
			if ( rootEntityCreation ) {
				Schema schema = survey.getSchema();
				schema.addRootEntityDefinition((EntityDefinition) editedNode);
			} else {
				
			}
			addTreeNode(editedNode);
		}
		selectedNode = editedNode;
	}

	private void copyCommonTempNodeProperties(NodeDefinition node) {
		String name = (String) tempNode.get("name");
		String description = (String) tempNode.get("name");
		Boolean multiple = (Boolean) tempNode.get("multiple");

		node.setName(name);
		node.setDescription(selectedLanguageCode, description);
		node.setMultiple(multiple);
		ModelVersion sinceVersion = (ModelVersion) tempNode.get("sinceVersion");
		if ( sinceVersion != null && sinceVersion != VERSION_EMPTY_SELECTION ) {
			node.setSinceVersion(sinceVersion);
		} else {
			node.setSinceVersion(null);
		}
		ModelVersion deprecatedVersion = (ModelVersion) tempNode.get("deprecatedVersion");
		if ( deprecatedVersion != null && deprecatedVersion != VERSION_EMPTY_SELECTION ) {
			node.setDeprecatedVersion(deprecatedVersion);
		} else {
			node.setDeprecatedVersion(null);
		}
	}
	
	protected void initTempNode(NodeDefinition node) {
		if ( node == null ) {
			tempNode = null;
		} else {
			tempNode = new HashMap<String, Object>();
			tempNode.put("name", node.getName());
			tempNode.put("headingLabel", node.getLabel(Type.HEADING, selectedLanguageCode));
			tempNode.put("instanceLabel", node.getLabel(Type.INSTANCE, selectedLanguageCode));
			tempNode.put("numberLabel", node.getLabel(Type.NUMBER, selectedLanguageCode));
			tempNode.put("description", node.getDescription(selectedLanguageCode));
			tempNode.put("multiple", node.isMultiple());
			tempNode.put("sinceVersion", node.getSinceVersion());
			tempNode.put("deprecatedVersion", node.getDeprecatedVersion());
		}
	}
	
	//TODO move this part into a Composer...
	
	private void initTreeModel() {
//		SurveyEditVM viewModel = (SurveyEditVM) getViewModel();
//		CollectSurvey survey = viewModel.getSurvey();
		List<EntityDefinition> rootDefns = survey.getSchema().getRootEntityDefinitions();
		List<TreeNode<NodeDefinition>> treeNodes = NodeDefinitionTreeNode.fromList(rootDefns);
		TreeNode<NodeDefinition> root = new NodeDefinitionTreeNode(null, treeNodes);
		treeModel = new DefaultTreeModel<NodeDefinition>(root);
	}
	
	protected void removeSelectedTreeNode() {
		int[] selectionPath = treeModel.getSelectionPath();
		TreeNode<NodeDefinition> treeNode = treeModel.getChild(selectionPath);
		TreeNode<NodeDefinition> parentTreeNode = treeNode.getParent();
		parentTreeNode.remove(treeNode);
	}
	
	protected void addTreeNode(NodeDefinition item) {
		NodeDefinitionTreeNode treeNode = new NodeDefinitionTreeNode(item);
		int[] selectionPath = treeModel.getSelectionPath();
		if ( selectionPath == null || item.getParentDefinition() == null ) {
			treeModel.getRoot().add(treeNode);
		} else {
			TreeNode<NodeDefinition> selectedTreeNode = treeModel.getChild(selectionPath);
			selectedTreeNode.add(treeNode);
		}
		treeModel.addOpenObject(treeNode.getParent());
		treeModel.setSelection(Arrays.asList(treeNode));
	}
	
	public DefaultTreeModel<NodeDefinition> getNodes() {
		if ( treeModel == null ) {
			initTreeModel();
		}
		return treeModel;
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

	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public Integer getAttributeTypeIndex() {
		return attributeTypeIndex;
	}

	public void setAttributeTypeIndex(Integer attributeTypeIndex) {
		this.attributeTypeIndex = attributeTypeIndex;
	}

	public boolean isRootEntityCreation() {
		return rootEntityCreation;
	}

	public boolean isNewNode() {
		return newNode;
	}

	public Map<String, Object> getTempNode() {
		return tempNode;
	}

	public void setTempNode(Map<String, Object> tempNode) {
		this.tempNode = tempNode;
	}

	public NodeDefinition getSelectedNode() {
		return selectedNode;
	}
	
}
