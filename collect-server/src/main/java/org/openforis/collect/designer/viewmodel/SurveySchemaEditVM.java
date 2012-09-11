/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.designer.form.AttributeDefinitionFormObject;
import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.collect.designer.form.NumericAttributeDefinitionFormObject;
import org.openforis.collect.designer.model.AttributeType;
import org.openforis.collect.designer.model.NodeType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.Form;
import org.zkoss.bind.SimpleForm;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveySchemaEditVM extends SurveyEditVM {

	private static final String SURVEY_EDIT_VERSIONING_POPUP_URL = "survey_edit_versioning_popup.zul";
	private static final String ATTRIBUTE_DEFAULTS_FIELD = "attributeDefaults";
	private static final String NUMBER_ATTRIBUTE_PRECISIONS_FIELD = "precisions";
	private static final String SURVEY_EDIT_CODE_LISTS_POPUP_URL = "survey_edit_code_lists_popup.zul";
	
	private DefaultTreeModel<NodeDefinition> treeModel;
	private NodeDefinition selectedNode;
	private Form tempFormObject;
	private NodeDefinitionFormObject<NodeDefinition> formObject;
	private String nodeType;
	private String attributeType;
	private boolean editingNode;
	private boolean newNode;
	private boolean rootEntityCreation;
	private List<AttributeDefault> attributeDefaults;
	private List<Precision> numericAttributePrecisions;
	
	@Command
	@NotifyChange({"editingNode","newNode","rootEntityCreation","nodeType","attributeType",
		"tempFormObject","formObject","attributeDefaults","numericAttributePrecisions"})
	public void nodeSelected(@BindingParam("node") Treeitem node) {
		if ( node != null ) {
			TreeNode<NodeDefinition> treeNode = node.getValue();
			selectedNode = treeNode.getData();
			editingNode = true;
		} else {
			selectedNode = null;
			editingNode = false;
		}
		newNode = false;
		rootEntityCreation = false;
		initFormObject(selectedNode);
	}
	
	@Command
	@NotifyChange({"editingNode","newNode","rootEntityCreation","nodeType","attributeType",
		"tempFormObject","formObject","attributeDefaults","numericAttributePrecisions"})
	public void addRootEntity() {
		editingNode = true;
		newNode = true;
		rootEntityCreation = true;
		nodeType = NodeType.ENTITY.name();
		selectedNode = null;
		initFormObject();
		Collection<NodeDefinitionTreeNode> emptySelection = Collections.emptyList();
		treeModel.setSelection(emptySelection);
	}
	
	@Command
	@NotifyChange({"editingNode","newNode","rootEntityCreation","nodeType","attributeType","tempFormObject","formObject",
		"attributeDefaults","numericAttributePrecisions"})
	public void addNode() throws Exception {
		if ( selectedNode != null && selectedNode instanceof EntityDefinition ) {
			editingNode = true;
			newNode = true;
			rootEntityCreation = false;
			nodeType = null;
			attributeType = null;
			formObject = null;
		} else {
			throw new Exception("Cannot add a child to an Attribute Definition");
		}
	}
	
	@Command
	@NotifyChange({"editingNode","newNode","rootEntityCreation","nodeType","attributeType","tempFormObject","formObject",
		"attributeDefaults","numericAttributePrecisions"})
	public void newNode(@BindingParam("nodeType") String nodeType, @BindingParam("attributeType") String attributeType) throws Exception {
		if ( selectedNode != null && selectedNode instanceof EntityDefinition ) {
			editingNode = true;
			newNode = true;
			rootEntityCreation = false;
			this.nodeType = nodeType;
			this.attributeType = attributeType;
			initFormObject();
		}
	}
	
	@Command
	@NotifyChange({"nodeType","tempFormObject","formObject"})
	public void nodeTypeChanged(@BindingParam("nodeType") String nodeType) {
		this.nodeType = nodeType;
		initFormObject();
	}

	@Command
	@NotifyChange({"attributeType","tempFormObject","formObject"})
	public void attributeTypeChanged(@BindingParam("attributeType") String attributeType) {
		this.attributeType = attributeType;
		initFormObject();
	}

	@Command
	@NotifyChange({"selectedNode","tempFormObject","formObject","newNode","rootEntityCreation"})
	public void applyChanges() {
		NodeDefinition editedNode;
		if ( newNode ) {
			editedNode = NodeType.createNodeDefinition(survey, nodeType, attributeType);
		} else {
			editedNode = selectedNode;
		}
		//TODO avoid the use of side effect...
		formObject.saveTo(editedNode, selectedLanguageCode);
		
		if ( newNode ) {
			if ( rootEntityCreation ) {
				CollectSurvey survey = getSurvey();
				Schema schema = survey.getSchema();
				schema.addRootEntityDefinition((EntityDefinition) editedNode);
			} else if ( selectedNode != null ) {
				if ( selectedNode instanceof EntityDefinition ) {
					( (EntityDefinition) selectedNode).addChildDefinition(editedNode);
				} else {
					throw new IllegalStateException("Trying to add a child to an Attribute");
				}
			} else {
				throw new IllegalStateException("No entity parent node selected");
			}
			appendTreeNodeToSelectedNode(editedNode);
		}
		selectedNode = editedNode;
		//initFormObject(selectedNode);
		newNode = false;
		rootEntityCreation = false;
	}

	@Command
	public void openVersioningManagerPopUp() {
		Window window = (Window) Executions.createComponents(
				SURVEY_EDIT_VERSIONING_POPUP_URL, null, null);
		window.doModal();
	}

	@Command
	public void openCodeListsManagerPopUp() {
		Window window = (Window) Executions.createComponents(
				SURVEY_EDIT_CODE_LISTS_POPUP_URL, null, null);
		window.doModal();
	}

	@Command
	@NotifyChange("attributeDefaults")
	public void addAttributeDefault() {
		if ( attributeDefaults == null ) {
			initAttributeDefaultsList();
		}
		AttributeDefault attributeDefault = new AttributeDefault();
		attributeDefaults.add(attributeDefault);
	}
	
	@Command
	@NotifyChange("attributeDefaults")
	public void deleteAttributeDefault(@BindingParam("attributeDefault") AttributeDefault attributeDefault) {
		attributeDefaults.remove(attributeDefault);
	}
	
	@Command
	@NotifyChange("numericAttributePrecisions")
	public void addNumericAttributePrecision() {
		if ( numericAttributePrecisions == null ) {
			initNumericAttributePrecisionsList();
		}
		Precision precision = new Precision();
		numericAttributePrecisions.add(precision);
	}
	
	@Command
	@NotifyChange("numericAttributePrecisions")
	public void deleteNumericAttributePrecision(@BindingParam("precision") Precision precision) {
		numericAttributePrecisions.remove(precision);
	}
	
	protected void initAttributeDefaultsList() {
		if ( attributeDefaults == null ) {
			attributeDefaults = new ArrayList<AttributeDefault>();
			tempFormObject.setField(ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
			((AttributeDefinitionFormObject<?>) formObject).setAttributeDefaults(attributeDefaults);
		}
	}
	
	protected void initNumericAttributePrecisionsList() {
		if ( numericAttributePrecisions == null ) {
			numericAttributePrecisions = new ArrayList<Precision>();
			tempFormObject.setField(NUMBER_ATTRIBUTE_PRECISIONS_FIELD, numericAttributePrecisions);
			((NumericAttributeDefinitionFormObject<?>) formObject).setPrecisions(numericAttributePrecisions);
		}
	}
	
	private void initFormObject() {
		NodeType nodeTypeEnum = null;
		AttributeType attributeTypeEnum = null;
		if ( nodeType != null ) {
			nodeTypeEnum = NodeType.valueOf(nodeType);
		}
		if ( attributeType != null ) {
			attributeTypeEnum = AttributeType.valueOf(attributeType);
		}
		formObject = NodeDefinitionFormObject.newInstance(nodeTypeEnum, attributeTypeEnum);
		tempFormObject = new SimpleForm();
		attributeDefaults = null;
		numericAttributePrecisions = null;
	}

	protected void initFormObject(NodeDefinition node) {
		calculateNodeType(node);
		initFormObject();
		formObject.loadFrom(node, selectedLanguageCode);
		if ( formObject instanceof AttributeDefinitionFormObject ) {
			attributeDefaults = ((AttributeDefinitionFormObject<?>) formObject).getAttributeDefaults();
			tempFormObject.setField(ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
			
			if ( formObject instanceof NumericAttributeDefinitionFormObject ) {
				numericAttributePrecisions = ((NumericAttributeDefinitionFormObject<?>) formObject).getPrecisions();
				tempFormObject.setField(NUMBER_ATTRIBUTE_PRECISIONS_FIELD, numericAttributePrecisions);
			}
		}
	}
	
	protected void calculateNodeType(NodeDefinition node) {
		NodeType nodeTypeEnum = NodeType.typeOf(node);
		nodeType = nodeTypeEnum.name();
		if ( nodeTypeEnum == NodeType.ATTRIBUTE) {
			AttributeType attributeTypeEnum = AttributeType.typeOf((AttributeDefinition) node);
			attributeType = attributeTypeEnum.name();
		} else {
			attributeType = null;
		}
	}
	
	//TODO move this part into a Composer...
	
	private void initTreeModel() {
//		SurveyEditVM viewModel = (SurveyEditVM) getViewModel();
//		CollectSurvey survey = viewModel.getSurvey();
		CollectSurvey survey = getSurvey();
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
	
	protected void appendTreeNodeToSelectedNode(NodeDefinition item) {
		NodeDefinitionTreeNode treeNode = new NodeDefinitionTreeNode(item);
		int[] selectionPath = treeModel.getSelectionPath();
		if ( selectionPath == null || item.getParentDefinition() == null ) {
			TreeNode<NodeDefinition> root = treeModel.getRoot();
			root.add(treeNode);
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

	public String getAttributeType() {
		return attributeType;
	}
	
	public boolean isRootEntityCreation() {
		return rootEntityCreation;
	}

	public boolean isNewNode() {
		return newNode;
	}

	public NodeDefinition getSelectedNode() {
		return selectedNode;
	}
	
	public NodeDefinitionFormObject<NodeDefinition> getFormObject() {
		return formObject;
	}

	public boolean isEditingNode() {
		return editingNode;
	}

	public Form getTempFormObject() {
		return tempFormObject;
	}

	public List<AttributeDefault> getAttributeDefaults() {
		return attributeDefaults;
	}
	
	public List<Precision> getNumericAttributePrecisions() {
		return numericAttributePrecisions;
	}
	
}
