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
import org.zkoss.bind.Form;
import org.zkoss.bind.SimpleForm;
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
	
	private NodeDefinition editedNode;
	
	private Map<String, Object> editedItem = new HashMap<String, Object>();
	
	private String nodeType;
	
	private Form entityForm = new SimpleForm();
	
	private Integer attributeTypeIndex;
	
	private enum AttributeTypes {
		BOOLEAN, CODE, COORDINATE, DATE, FILE, NUMBER, RANGE, TAXON, TEXT, TIME
	}
	
	@NotifyChange({"editedChildItem","editingChildItem","childItemLabel","childItemDescription","childItemQualifiable","childItemSinceVersion","childItemDeprecatedVersion"})
	@Command
	public void nodeSelected(@BindingParam("node") Treeitem node) {
		if ( node != null ) {
			TreeNode<NodeDefinition> treeNode = node.getValue();
			setEditedNode(treeNode.getData());
		} else {
			setEditedNode(null);
		}
	}
	
	@NotifyChange({"nodes","editedNode","editingNode","editedNodeHeadingLabel","editedNodeInstanceLabel","editedNodeNumberLabel","editedNodeDescription"})
	@Command
	public void addRootEntity() {
		Schema schema = survey.getSchema();
		EntityDefinition defn = new EntityDefinition();
		schema.addRootEntityDefinition(defn);
		addTreeNode(defn);
		setEditedNode(defn);
	}
	
	@NotifyChange({"nodes","editedNode","editingNode","editedNodeHeadingLabel","editedNodeInstanceLabel","editedNodeNumberLabel","editedNodeDescription"})
	@Command
	public void addNode() {
		if ( editedNode != null && editedNode instanceof EntityDefinition ) {
		}
	}
	
	@Command
	public void saveNode() {
		if ( NODE_TYPE_ENTITY.equals(nodeType) ) {
			editedNode = new EntityDefinition();
		} else {
		}
		copyCommonNodeProperties();
	}

	private void copyCommonNodeProperties() {
		String name = (String) editedItem.get("name");
		String description = (String) editedItem.get("name");
		Boolean multiple = (Boolean) editedItem.get("multiple");

		editedNode.setName(name);
		editedNode.setDescription(selectedLanguageCode, description);
		editedNode.setMultiple(multiple);
		ModelVersion sinceVersion = (ModelVersion) editedItem.get("sinceVersion");
		if ( sinceVersion != null && sinceVersion != VERSION_EMPTY_SELECTION ) {
			editedNode.setSinceVersion(sinceVersion);
		} else {
			editedNode.setSinceVersion(null);
		}
		ModelVersion deprecatedVersion = (ModelVersion) editedItem.get("deprecatedVersion");
		if ( deprecatedVersion != null && deprecatedVersion != VERSION_EMPTY_SELECTION ) {
			editedNode.setDeprecatedVersion(deprecatedVersion);
		} else {
			editedNode.setDeprecatedVersion(null);
		}
	}
	
	public NodeDefinition getEditedNode() {
		return editedNode;
	}

	public void setEditedNode(NodeDefinition editedNode) {
		this.editedNode = editedNode;
	}

	public boolean isEditingNode() {
		return this.editedNode != null;
	}
	
	public String getEditedNodeHeadingLabel() {
		return getEditedNodeLabel(Type.HEADING);
	}

	public void setEditedNodeHeadingLabel(String label) {
		setEditedNodeLabel(Type.HEADING, label);
	}
	
	public String getEditedNodeInstanceLabel() {
		return getEditedNodeLabel(Type.INSTANCE);
	}

	public void setEditedNodeInstanceLabel(String label) {
		setEditedNodeLabel(Type.INSTANCE, label);
	}

	public String getEditedNodeNumberLabel() {
		return getEditedNodeLabel(Type.NUMBER);
	}

	public void setEditedNodeNumberLabel(String label) {
		setEditedNodeLabel(Type.NUMBER, label);
	}

	protected String getEditedNodeLabel(Type type) {
		return editedNode != null ? editedNode.getLabel(type, selectedLanguageCode): null;
	}
	
	private void setEditedNodeLabel(Type type, String label) {
		if ( editedNode != null ) {
			editedNode.setLabel(type, selectedLanguageCode, label);
		}
	}
	
	public String getEditedNodeDescription() {
		return editedNode != null ? editedNode.getDescription(selectedLanguageCode): null;
	}

	public void setEditedNodeDescription(String description) {
		if ( editedNode != null ) {
			editedNode.setDescription(selectedLanguageCode, description);
		}
	}

	public boolean isEditedNodeMultiple() {
		return editedNode != null ? editedNode.isMultiple(): false;
	}

	public void setEditedNodeMultiple(boolean value) {
		if ( editedNode != null ) {
			editedNode.setMultiple(value);
		}
	}
	public ModelVersion getEditedNodeSinceVersion() {
		return editedNode != null ? editedNode.getSinceVersion(): null;
	}
	
	public void setEditedNodeSinceVersion(ModelVersion value) {
		if ( editedNode != null ) {
			ModelVersion modelVersion = value == VERSION_EMPTY_SELECTION ? null: value;
			editedNode.setSinceVersion(modelVersion);
		}
	}

	public ModelVersion getEditedNodeDeprecatedVersion() {
		return editedNode != null ? editedNode.getDeprecatedVersion(): null;
	}

	public void setEditedNodeDeprecatedVersion(ModelVersion value) {
		if ( editedNode != null  ) {
			ModelVersion modelVersion = value == VERSION_EMPTY_SELECTION ? null: value;
			editedNode.setDeprecatedVersion(modelVersion);
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

	public Form getEntityForm() {
		return entityForm;
	}

	public Integer getAttributeTypeIndex() {
		return attributeTypeIndex;
	}

	public void setAttributeTypeIndex(Integer attributeTypeIndex) {
		this.attributeTypeIndex = attributeTypeIndex;
	}

	public Map<String, Object> getEditedItem() {
		return editedItem;
	}

	public void setEditedItem(Map<String, Object> editedItem) {
		this.editedItem = editedItem;
	}

	
}
