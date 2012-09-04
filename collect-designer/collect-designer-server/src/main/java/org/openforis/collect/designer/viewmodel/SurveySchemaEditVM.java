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
import org.openforis.collect.designer.form.BooleanAttributeDefinitionFormObject;
import org.openforis.collect.designer.form.CodeAttributeDefinitionFormObject;
import org.openforis.collect.designer.form.EntityDefinitionFormObject;
import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.collect.designer.form.NumberAttributeDefinitionFormObject;
import org.openforis.collect.designer.form.NumericAttributeDefinitionFormObject;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
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

	private static final String ATTRIBUTE_DEFAULTS_FIELD = "attributeDefaults";
	private static final String NUMBER_ATTRIBUTE_PRECISIONS_FIELD = "precisions";

	enum NodeType {
		ENTITY, ATTRIBUTE
	}
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
	
	private enum AttributeType {
		BOOLEAN, CODE, COORDINATE, DATE, FILE, NUMBER, RANGE, TAXON, TEXT, TIME
	}
	
	@NotifyChange({"editingNode","newNode","rootEntityCreation","nodeType","attributeType",
		"tempFormObject","formObject","attributeDefaults","numericAttributePrecisions"})
	@Command
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
	
	@NotifyChange({"editingNode","tempFormObject","formObject","newNode","rootEntityCreation","nodeType",
		"attributeType","attributeDefaults","numericAttributePrecisions"})
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
	
	@NotifyChange({"tempFormObject","formObject","newNode","rootEntityCreation","nodeType","attributeType",
		"attributeDefaults","numericAttributePrecisions"})
	@Command
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
	
	@NotifyChange({"selectedNode","tempFormObject","formObject","newNode","rootEntityCreation"})
	@Command
	public void saveNode() {
		NodeDefinition editedNode;
		if ( newNode ) {
			editedNode = createNodeDefinition();
		} else {
			editedNode = selectedNode;
		}
		formObject.copyValues(editedNode, selectedLanguageCode);
		
		if ( newNode ) {
			if ( rootEntityCreation ) {
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
		initFormObject(selectedNode);
		newNode = false;
		rootEntityCreation = false;
	}

	@NotifyChange("attributeDefaults")
	@Command
	public void addAttributeDefault() {
		if ( attributeDefaults == null ) {
			initAttributeDefaultsList();
		}
		AttributeDefault attributeDefault = new AttributeDefault();
		attributeDefaults.add(attributeDefault);
	}
	
	@NotifyChange("attributeDefaults")
	@Command
	public void deleteAttributeDefault(@BindingParam("attributeDefault") AttributeDefault attributeDefault) {
		attributeDefaults.remove(attributeDefault);
	}
	
	@NotifyChange("numericAttributePrecisions")
	@Command
	public void addNumericAttributePrecision() {
		if ( numericAttributePrecisions == null ) {
			initNumericAttributePrecisionsList();
		}
		Precision precision = new Precision();
		numericAttributePrecisions.add(precision);
	}
	
	@NotifyChange("numericAttributePrecisions")
	@Command
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
	
	private NodeDefinition createNodeDefinition() {
		NodeDefinition result;
		NodeType nodeTypeEnum = NodeType.valueOf(nodeType);
		switch(nodeTypeEnum) {
		case ENTITY:
			result = new EntityDefinition();
			break;
		case ATTRIBUTE:
			AttributeType attrType = AttributeType.valueOf(attributeType);
			switch(attrType) {
			case BOOLEAN:
				result = new BooleanAttributeDefinition();
				break;
			case CODE:
				result = new CodeAttributeDefinition();
				break;
			case COORDINATE:
				result = new CoordinateAttributeDefinition();
				break;
			case DATE:
				result = new DateAttributeDefinition();
				break;
			case FILE:
				result = new FileAttributeDefinition();
				break;
			case NUMBER:
				result = new NumberAttributeDefinition();
				break;
			case RANGE:
				result = new RangeAttributeDefinition();
				break;
			case TAXON:
				result = new TaxonAttributeDefinition();
				break;
			case TEXT:
				result = new TextAttributeDefinition();
				break;
			case TIME:
				result = new TimeAttributeDefinition();
				break;
			default:
				throw new IllegalStateException("Attribute type not supported: " + attributeType);
			}
			break;
		default:
			throw new IllegalStateException("Node type not supported: " + nodeType);
		}
		result.setSchema(survey.getSchema());
		return result;
	}

	@NotifyChange({"nodeType","tempFormObject","formObject"})
	@Command
	public void nodeTypeChanged(@BindingParam("nodeType") String nodeType) {
		this.nodeType = nodeType;
		initFormObject();
	}

	@NotifyChange({"attributeType","tempFormObject","formObject"})
	@Command
	public void attributeTypeChanged(@BindingParam("attributeType") String attributeType) {
		this.attributeType = attributeType;
		initFormObject();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initFormObject() {
		if ( nodeType != null ) {
			NodeType nodeTypeEnum = NodeType.valueOf(nodeType);
			switch ( nodeTypeEnum ) {
			case ENTITY:
				formObject = new EntityDefinitionFormObject();
				break;
			case ATTRIBUTE:
				if ( attributeType != null ) {
					AttributeType attrType = AttributeType.valueOf(attributeType);
					switch (attrType) {
					case BOOLEAN:
						formObject = new BooleanAttributeDefinitionFormObject();
						break;
					case CODE:
						formObject = new CodeAttributeDefinitionFormObject();
						break;
					case NUMBER:
						formObject = new NumberAttributeDefinitionFormObject();
						break;
					default:
						throw new IllegalStateException("Attribute type not supported");
					}
				} else {
					formObject = null;
				}
				break;
			}
		} else {
			formObject = null;
		}
		tempFormObject = new SimpleForm();
		attributeDefaults = null;
		numericAttributePrecisions = null;
	}

	protected void initFormObject(NodeDefinition node) {
		calculateNodeType(node);
		initFormObject();
		formObject.setValues(node, selectedLanguageCode);
		if ( formObject instanceof AttributeDefinitionFormObject ) {
			attributeDefaults = ((AttributeDefinitionFormObject<?>) formObject).getAttributeDefaults();
			tempFormObject.setField(ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
			
			if ( formObject instanceof NumericAttributeDefinitionFormObject ) {
				numericAttributePrecisions = ((NumericAttributeDefinitionFormObject<?>) formObject).getPrecisions();
				tempFormObject.setField(NUMBER_ATTRIBUTE_PRECISIONS_FIELD, numericAttributePrecisions);
			}
		}
	}
	
	private void calculateNodeType(NodeDefinition node) {
		if ( node instanceof EntityDefinition ) {
			nodeType = NodeType.ENTITY.name();
		} else {
			nodeType = NodeType.ATTRIBUTE.name();
			if ( node instanceof BooleanAttributeDefinition ) {
				attributeType = AttributeType.BOOLEAN.name();
			} else if ( node instanceof CodeAttributeDefinition ) {
				attributeType = AttributeType.CODE.name();
			} else if ( node instanceof CoordinateAttributeDefinition ) {
				attributeType = AttributeType.COORDINATE.name();
			} else if ( node instanceof DateAttributeDefinition ) {
				attributeType = AttributeType.DATE.name();
			} else if ( node instanceof FileAttributeDefinition ) {
				attributeType = AttributeType.FILE.name();
			} else if ( node instanceof NumberAttributeDefinition ) {
				attributeType = AttributeType.NUMBER.name();
			} else if ( node instanceof RangeAttributeDefinition ) {
				attributeType = AttributeType.RANGE.name();
			} else if ( node instanceof TaxonAttributeDefinition ) {
				attributeType = AttributeType.TAXON.name();
			} else if ( node instanceof TextAttributeDefinition ) {
				attributeType = AttributeType.TEXT.name();
			} else if ( node instanceof TimeAttributeDefinition ) {
				attributeType = AttributeType.TIME.name();
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
