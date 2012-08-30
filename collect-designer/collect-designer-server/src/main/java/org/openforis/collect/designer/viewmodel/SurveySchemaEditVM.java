/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.designer.form.EntityDefinitionFormObject;
import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
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
	
	private NodeDefinitionFormObject formObject;
	
	private String nodeType;
	
	private String attributeType;
	
	private boolean rootEntityCreation;
	
	private boolean newNode;
	
	private enum AttributeTypes {
		BOOLEAN, CODE, COORDINATE, DATE, FILE, NUMBER, RANGE, TAXON, TEXT, TIME
	}
	
	@NotifyChange({"formObject","newNode","rootEntityCreation"})
	@Command
	public void nodeSelected(@BindingParam("node") Treeitem node) {
		if ( node != null ) {
			TreeNode<NodeDefinition> treeNode = node.getValue();
			selectedNode = treeNode.getData();
		} else {
			selectedNode = null;
		}
		newNode = false;
		rootEntityCreation = false;
		initFormObject(selectedNode);
	}
	
	@NotifyChange({"formObject","newNode","rootEntityCreation","nodeType"})
	@Command
	public void addRootEntity() {
		newNode = true;
		rootEntityCreation = true;
		nodeType = NODE_TYPE_ENTITY;
		selectedNode = null;
		formObject = new EntityDefinitionFormObject();
		Collection<NodeDefinitionTreeNode> emptySelection = Collections.emptyList();
		treeModel.setSelection(emptySelection);
	}
	
	@NotifyChange({"formObject","newNode","rootEntityCreation","nodeType"})
	@Command
	public void addNode() throws Exception {
		if ( selectedNode != null && selectedNode instanceof EntityDefinition ) {
			formObject = null;
			rootEntityCreation = false;
			newNode = true;
			nodeType = null;
		} else {
			throw new Exception("Cannot add a child to an Attribute Definition");
		}
	}
	
	@NotifyChange({"selectedNode","formObject","newNode","rootEntityCreation"})
	@Command
	public void saveNode() {
		EntityDefinition editedNode;
		if ( newNode && NODE_TYPE_ENTITY.equals(nodeType) ) {
			editedNode = new EntityDefinition();
		} else {
			//TODO
			editedNode = null;
		}
		formObject.copyValues(editedNode, selectedLanguageCode);
		
		if ( newNode ) {
			if ( rootEntityCreation ) {
				Schema schema = survey.getSchema();
				schema.addRootEntityDefinition((EntityDefinition) editedNode);
			} else if ( selectedNode != null && selectedNode instanceof EntityDefinition ) {
				( (EntityDefinition) selectedNode).addChildDefinition(editedNode);
			}
			addTreeNodeToSelectedNode(editedNode);
		}
		selectedNode = editedNode;
	}

	@NotifyChange({"nodeType","formObject"})
	@Command
	public void nodeTypeChanged(@BindingParam("nodeType") String nodeType) {
		this.nodeType = nodeType;
		initFormObject();
	}

	@NotifyChange({"attributeType","formObject"})
	@Command
	public void attributeTypeChanged(@BindingParam("attributeType") String attributeTypeIndex) {
		this.attributeType = attributeTypeIndex;
		initFormObject();
	}

	private void initFormObject() {
		if ( NODE_TYPE_ENTITY.equals(nodeType)) {
			formObject = new EntityDefinitionFormObject();
		}
	}

	protected void initFormObject(NodeDefinition node) {
		calculateNodeType(node);
		initFormObject();
		formObject.setValues(node, selectedLanguageCode);
	}
	
	private void calculateNodeType(NodeDefinition node) {
		if ( node instanceof EntityDefinition ) {
			nodeType = NODE_TYPE_ENTITY;
		} else {
			nodeType = NODE_TYPE_ATTRIBUTE;
			if ( node instanceof BooleanAttributeDefinition ) {
				attributeType = AttributeTypes.BOOLEAN.name();
			} else if ( node instanceof CodeAttributeDefinition ) {
				attributeType = AttributeTypes.CODE.name();
			} else if ( node instanceof CoordinateAttributeDefinition ) {
				attributeType = AttributeTypes.COORDINATE.name();
			} else if ( node instanceof DateAttributeDefinition ) {
				attributeType = AttributeTypes.DATE.name();
			} else if ( node instanceof FileAttributeDefinition ) {
				attributeType = AttributeTypes.FILE.name();
			} else if ( node instanceof NumberAttributeDefinition ) {
				attributeType = AttributeTypes.NUMBER.name();
			} else if ( node instanceof RangeAttributeDefinition ) {
				attributeType = AttributeTypes.RANGE.name();
			} else if ( node instanceof TaxonAttributeDefinition ) {
				attributeType = AttributeTypes.TAXON.name();
			} else if ( node instanceof TextAttributeDefinition ) {
				attributeType = AttributeTypes.TEXT.name();
			} else if ( node instanceof TimeAttributeDefinition ) {
				attributeType = AttributeTypes.TIME.name();
			}				
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
	
	protected void addTreeNodeToSelectedNode(NodeDefinition item) {
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
	
	public NodeDefinitionFormObject getFormObject() {
		return formObject;
	}
	
}
