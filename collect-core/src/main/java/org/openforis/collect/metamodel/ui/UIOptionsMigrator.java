package org.openforis.collect.metamodel.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.ui.UIOptions.CoordinateAttributeFieldsOrder;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.metamodel.ui.UITable.Direction;
import org.openforis.collect.metamodel.ui.UITextField.TextTransform;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TextAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class UIOptionsMigrator {
	
	public UIConfiguration migrateToUIConfiguration(UIOptions oldUIOptions) throws UIOptionsMigrationException {
		CollectSurvey survey = oldUIOptions.getSurvey();
		UIConfiguration result = new UIConfiguration(survey);
		List<UITabSet> tabSets = oldUIOptions.getTabSets();
		for (UITabSet tabSet : tabSets) {
			UIFormSet formSet;
			EntityDefinition associatedRootEntity = findAssociatedRootEntity(tabSet);
			if ( associatedRootEntity == null ) {
				throw new UIOptionsMigrationException("Cannot find associated root entity. Tab set: " + tabSet.getName());
			} else {
				formSet = result.createFormSet();
				formSet.setRootEntityDefinition(associatedRootEntity);
				result.addFormSet(formSet);
			}
			List<UITab> tabs = tabSet.getTabs();
			for (UITab tab : tabs) {
				createForm(formSet, tab);
			}
		}
		verifyMigration(result);
		return result;
	}

	private void verifyMigration(final UIConfiguration uiConfig) throws UIOptionsMigrationException {
//		
//		StringWriter writer = new StringWriter();
//		UIConfigurationSerializer serializer = new UIConfigurationSerializer();
//		serializer.write(uiConfig, writer);
//		System.out.println(writer.toString());
//		
		final CollectSurvey survey = uiConfig.getSurvey();
		Schema schema = survey.getSchema();
		schema.traverse(new NodeDefinitionVisitor() {
			@Override
			public void visit(NodeDefinition definition) {
				int nodeId = definition.getId();
				UIModelObject uiModelObj = uiConfig.getModelObjectByNodeDefinitionId(nodeId);
				if ( uiModelObj == null ) {
					throw new UIOptionsMigrationException(String.format("No UI model object found for node with id %d in %s survey with uri %s", nodeId, 
							survey.isTemporary() ? "temporary": "published", survey.getUri()));
				}
			}
		});

	}

	protected void createForm(UIFormContentContainer parent, UITab tab) throws UIOptionsMigrationException {
		UIOptions oldUIOptions = tab.getUIOptions();
		UIForm form = parent.createForm();
		copyLabels(tab, form);

		//create form components
		List<NodeDefinition> childNodes = oldUIOptions.getNodesPerTab(tab, false);
		int lastCol = 1;
		int currentRow = 0;
		for (NodeDefinition nodeDefn : childNodes) {
			int childCol = oldUIOptions.getColumn(nodeDefn);
			if (childCol <= lastCol) {
				currentRow ++;
			}
			addFormComponent(form, nodeDefn, currentRow);
			lastCol = childCol;
		}
		
		//create inner forms
		List<UITab> nestedTabs = new ArrayList<UITab>();
		for (UITab childTab : tab.getTabs()) {
			boolean toBeAdded = true;
			List<NodeDefinition> innerNodes = oldUIOptions.getNodesPerTab(childTab, false);
			for (NodeDefinition nestedTabChildNode : innerNodes) {
				for (NodeDefinition childDefn : childNodes ) {
					if ( childDefn == nestedTabChildNode || 
							(childDefn instanceof EntityDefinition && nestedTabChildNode.isDescendantOf((EntityDefinition) childDefn) ) ) {
						toBeAdded = false;
						break;
					}
				}
			}
			if ( toBeAdded ) {
				nestedTabs.add(childTab);
			}
		}
		
		for (UITab uiTab : nestedTabs) {
			createForm(form, uiTab);
		}
		
		parent.addForm(form);
	}

	private void createInnerForms(UITab tab, UIFormSection parent) throws UIOptionsMigrationException {
//		UIOptions oldUIOptions = tab.getUIOptions();
//		List<NodeDefinition> nodesPerTab = oldUIOptions.getNodesPerTab(tab, false);
//		for (NodeDefinition nodeDefn : nodesPerTab) {
//			if ( nodeDefn instanceof EntityDefinition && nodeDefn.isMultiple() && 
//					oldUIOptions.getLayout((EntityDefinition) nodeDefn) == Layout.FORM ) {
//				UITab assignedTab = oldUIOptions.getAssignedTab(nodeDefn);
//				if ( assignedTab == tab ) {
//					createFormFromFormEntity(assignedTab, parent, (EntityDefinition) nodeDefn);
//				}
//			}
//		}
		List<UITab> innerTabs = tab.getTabs();
		for (UITab innerTab : innerTabs) {
			createForm(parent, innerTab);
		}
	}

//	protected void createFormFromFormEntity(UITab parentTab, Form parent, EntityDefinition entityDefn) {
//		Form form = parent.createForm();
//		copyLabels(entityDefn, form);
//		form.setEntityId(entityDefn.getId());
//		form.setMultiple(true);
//		createMainFormSection(form, parentTab.getUIOptions(), entityDefn.getChildDefinitions());
//		createInnerForms(parentTab, form);
//		parent.addForm(form);
//	}

	protected EntityDefinition findAssignedEntityDefinition(UITab tab) {
		UIOptions oldUIOptions = tab.getUIOptions();
		List<NodeDefinition> nodesPerTab = oldUIOptions.getNodesPerTab(tab, false);
		if ( nodesPerTab.size() == 1 ) {
			NodeDefinition firstNode = nodesPerTab.get(0);
			if ( firstNode instanceof EntityDefinition ) {
				return (EntityDefinition) firstNode;
			}
		}
		return null;
	}

	protected void addFormComponent(UIFormContentContainer parent, NodeDefinition nodeDefn, int row) throws UIOptionsMigrationException {
		CollectSurvey survey = (CollectSurvey) nodeDefn.getSurvey();
		UIOptions oldUIOptions = survey.getUIOptions();
		UIFormComponent component;
		if ( nodeDefn instanceof AttributeDefinition ) {
			component = createField(parent, nodeDefn);
		} else {
			EntityDefinition entityDefn = (EntityDefinition) nodeDefn;
			if ( entityDefn.isMultiple() && oldUIOptions.getLayout(entityDefn) == Layout.TABLE ) {
				component = createTable(parent, entityDefn);
			} else {
				component = createFormSection(parent, entityDefn);
			}
		}
		component.setColumn(oldUIOptions.getColumn(nodeDefn));
		component.setColumnSpan(oldUIOptions.getColumnSpan(nodeDefn));
		component.setRow(row);
		component.setHideWhenNotRelevant(oldUIOptions.isHideWhenNotRelevant(nodeDefn));
		parent.addChild(component);
	}

	protected UIField createField(UIFormContentContainer parent, NodeDefinition nodeDefn) {
		CollectSurvey survey = (CollectSurvey) nodeDefn.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		CollectAnnotations annotations = survey.getAnnotations();
		UIField field;
		if (nodeDefn instanceof CodeAttributeDefinition) {
			UICodeField codeField = parent.createCodeField();
			CodeAttributeDefinition codeAttrDefn = (CodeAttributeDefinition) nodeDefn;
			codeField.setLayout(uiOptions.getLayoutType(codeAttrDefn));
			codeField.setShowCode(uiOptions.getShowCode(codeAttrDefn));
			codeField.setItemsOrientation(uiOptions.getLayoutDirection(codeAttrDefn));
			field = codeField;
		} else if (nodeDefn instanceof TextAttributeDefinition) {
			UITextField textField = parent.createTextField();
			textField.setAutoCompleteGroup(annotations.getAutoCompleteGroup((TextAttributeDefinition) nodeDefn));
			TextTransform textTranform = uiOptions.isAutoUppercase((TextAttributeDefinition) nodeDefn)
					? TextTransform.UPPERCASE
					: TextTransform.NONE;
			textField.setTextTranform(textTranform);
			field = textField;
		} else {
			field = parent.createField();
		}
		field.setAttributeDefinitionId(nodeDefn.getId());
		
		if ( nodeDefn instanceof CoordinateAttributeDefinition ) {
			CoordinateAttributeFieldsOrder fieldsOrder = uiOptions.getFieldsOrder((CoordinateAttributeDefinition) nodeDefn);
			field.setFieldsOrder(fieldsOrder);
		}
		field.setHidden(uiOptions.isHidden(nodeDefn));
		return field;
	}

	protected UIFormSection createFormSection(UIFormContentContainer parent, EntityDefinition entityDefn) throws UIOptionsMigrationException {
		CollectSurvey survey = (CollectSurvey) entityDefn.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		UITab parentTab = uiOptions.getAssignedTab(entityDefn, true);
		
		UIFormSection formSection = parent.createFormSection();
		formSection.setEntityDefinition(entityDefn);

		int currentRow = 0;
		int lastCol = 1;
		List<NodeDefinition> childDefns = entityDefn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefns) {
			UITab assignedChildTab = uiOptions.getAssignedTab(childDefn, true);
			if ( assignedChildTab == parentTab ) {
				int childCol = uiOptions.getColumn(childDefn);
				if (childCol <= lastCol) {
					currentRow ++;
				}
				addFormComponent(formSection, childDefn, currentRow);
				lastCol = childCol;
			}
		}
		
		//create inner tabs
		for (NodeDefinition innerChildDefn : childDefns) {
			UITab assignedInnerTab = uiOptions.getAssignedTab(innerChildDefn);
			if ( assignedInnerTab != null && assignedInnerTab.isDescendantOf(parentTab) ) {
				createInnerForms(parentTab, formSection);
				break;
			}
		}
		return formSection;
	}

	protected UITable createTable(UIFormContentContainer section, EntityDefinition entityDefn) throws UIOptionsMigrationException {
		UITable table = section.createTable();
		table.setEntityDefinition(entityDefn);
		CollectSurvey survey = (CollectSurvey) entityDefn.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		table.setCountInSummaryList(uiOptions.getCountInSumamryListValue(entityDefn));
		table.setDirection(Direction.valueOf(uiOptions.getDirection(entityDefn).toString()));
		table.setShowRowNumbers(uiOptions.getShowRowNumbersValue(entityDefn));
		List<NodeDefinition> childDefinitions = entityDefn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefinitions) {
			UITableHeadingComponent component = createTableHeadingComponent(table, childDefn);
			table.addHeadingComponent(component);
		}
		return table;
	}
	
	protected UITableHeadingComponent createTableHeadingComponent(UITableHeadingContainer parent, NodeDefinition nodeDefn) throws UIOptionsMigrationException {
		UITableHeadingComponent component;
		if ( nodeDefn instanceof EntityDefinition ) {
			EntityDefinition entityDefn = (EntityDefinition) nodeDefn;
			if ( entityDefn.isMultiple() ) {
				throw new UIOptionsMigrationException("Nested multiple entity inside table layout entity is not supported: " + nodeDefn.getPath());
			}
			component = parent.createColumnGroup();
			UIColumnGroup columnGroup = (UIColumnGroup) component;
			columnGroup.setEntityDefinition(entityDefn);
			
			List<NodeDefinition> innerChildDefns = entityDefn.getChildDefinitions();
			for (NodeDefinition innerChildDefn : innerChildDefns) {
				UITableHeadingComponent innerComponent = createTableHeadingComponent(columnGroup, innerChildDefn);
				columnGroup.addHeadingComponent(innerComponent);
			}
		} else {
			component = parent.createColumn();
			((UIColumn) component).setAttributeDefinition((AttributeDefinition) nodeDefn);
		}
		return component;
	}

	protected EntityDefinition findAssociatedRootEntity(UITabSet tabSet) {
		UIOptions uiOptions = tabSet.getUIOptions();
		CollectSurvey survey = uiOptions.getSurvey();
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		for (EntityDefinition rootEntityDefn : rootEntityDefinitions) {
			UITabSet assignedRootTabSet = uiOptions.getAssignedRootTabSet(rootEntityDefn);
			if ( ObjectUtils.equals(assignedRootTabSet, tabSet) ) {
				return rootEntityDefn;
			}
		}
		return null;
	}

	protected void copyLabels(UITab tab, UIForm form) {
		List<LanguageSpecificText> labels = tab.getLabels();
		for (LanguageSpecificText lst : labels) {
			form.setLabel(lst.getLanguage(), lst.getText());
		}
	}

	protected void copyLabels(EntityDefinition entityDefn, UIForm form) {
		List<NodeLabel> labels = getLabelsByType(entityDefn, Type.HEADING);
		for (LanguageSpecificText lst : labels) {
			form.setLabel(lst.getLanguage(), lst.getText());
		}
	}

	protected List<NodeLabel> getLabelsByType(NodeDefinition nodeDefn, NodeLabel.Type type) {
		List<NodeLabel> result = new ArrayList<NodeLabel>();
		List<NodeLabel> labels = nodeDefn.getLabels();
		for (NodeLabel label : labels) {
			if ( label.getType() == type ) {
				result.add(label);
			}
		}
		return result;
	}
	
	public static class UIOptionsMigrationException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		
		public UIOptionsMigrationException(String message) {
			super(message);
		}
		
	}

}
