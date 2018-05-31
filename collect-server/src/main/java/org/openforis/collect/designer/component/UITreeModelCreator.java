package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openforis.collect.designer.component.SchemaTreeModel.SchemaNodeData;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaTreeNode;
import org.openforis.collect.designer.util.Predicate;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyObject;

/**
 * 
 * @author S. Ricci
 *
 */
public class UITreeModelCreator extends SurveyObjectTreeModelCreator {

	public UITreeModelCreator(SurveyManager surveyManager, User loggedUser, ModelVersion version,
			Predicate<SurveyObject> includeNodePredicate,
			boolean includeRootEntity, boolean includeEmptyNodes, String labelLanguage) {
		this(surveyManager, loggedUser, version, null, includeNodePredicate, includeRootEntity, includeEmptyNodes, labelLanguage);
	}

	public UITreeModelCreator(SurveyManager surveyManager, User loggedUser, ModelVersion version,
			Predicate<SurveyObject> disabledNodePredicate,
			Predicate<SurveyObject> includeNodePredicate,
			boolean includeRootEntity, boolean includeEmptyNodes, String labelLanguage) {
		super(surveyManager, loggedUser, version, disabledNodePredicate, includeNodePredicate, includeRootEntity, includeEmptyNodes, labelLanguage);
	}

	@Override
	protected List<SchemaTreeNode> createChildNodes(SurveyObject surveyObject) {
		List<SchemaTreeNode> childNodes = new ArrayList<SchemaTreeNode>();
		if ( surveyObject instanceof EntityDefinition ) {
			List<SchemaTreeNode> entityChildrenNodes = createChildNodes((EntityDefinition) surveyObject);
			childNodes.addAll(entityChildrenNodes);
		} else if ( surveyObject instanceof UITab ) {
			List<SchemaTreeNode> childTabNodes = createChildNodes((UITab) surveyObject);
			childNodes.addAll(childTabNodes);
		} else if ( surveyObject instanceof AttributeDefinition ) {
			//no nested nodes for attributes
			childNodes = null;
		}
		return childNodes;
	}

	private List<SchemaTreeNode> createChildNodes(EntityDefinition entityDefn) {
		List<SchemaTreeNode> childNodes = new ArrayList<SchemaTreeNode>();
		
		CollectSurvey survey = (CollectSurvey) entityDefn.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		UITab assignedTab = uiOptions.getAssignedTab(entityDefn);
		
		//include node definitions
		List<NodeDefinition> childDefns = entityDefn.getChildDefinitions();
		Collection<? extends SchemaTreeNode> schemaTreeNodes = createNodes(assignedTab, childDefns);
		childNodes.addAll(schemaTreeNodes);
		
		//include tabs
		if ( entityDefn.isMultiple() && uiOptions.getLayout(entityDefn) == Layout.FORM ) {
			List<UITab> tabs = uiOptions.getTabsAssignableToChildren(entityDefn, false);
			Collection<? extends SchemaTreeNode> tabNodes = createNodes(tabs);
			childNodes.addAll(tabNodes);
		}
		return childNodes;
	}

	@Override
	protected List<SchemaTreeNode> createFirstLevelNodes(EntityDefinition rootEntity) {
		List<SchemaTreeNode> firstLevelTreeNodes = new ArrayList<SchemaTreeNode>();
		if (includeRootEntity) {
			SchemaTreeNode node = createRootNode(rootEntity);
			if ( node != null ) {
				firstLevelTreeNodes.add(node);
			}
		} else {
			CollectSurvey survey = (CollectSurvey) rootEntity.getSurvey();
			UIOptions uiOptions = survey.getUIOptions();
			UITabSet tabSet = uiOptions.getAssignedRootTabSet(rootEntity);
			for (UITab tab : tabSet.getTabs()) {
				SchemaTreeNode node = createNode(tab);
				if ( node != null ) {
					firstLevelTreeNodes.add(node);
				}
			}
		}
		return firstLevelTreeNodes;
	}
	
	private SchemaTreeNode createRootNode(EntityDefinition rootEntity) {
		SchemaNodeData data = new SchemaNodeData(rootEntity, rootEntity.getName(), false, false);
		SchemaTreeNode treeNode = (SchemaTreeNode) createNode(data, false);
		return treeNode;
	}

	private List<SchemaTreeNode> createChildNodes(UITab tab) {
		List<SchemaTreeNode> result = new ArrayList<SchemaTreeNode>();
		//add schema node definition tree nodes
		UIOptions uiOptions = tab.getUIOptions();
		List<NodeDefinition> childDefns = uiOptions.getNodesPerTab(tab, false);
		List<SchemaTreeNode> childSchemaNodes = createNodes(tab, childDefns);
		result.addAll(childSchemaNodes);
		
		//add children unassigned tab tree nodes
//		List<UITab> unassignedTabs = new ArrayList<UITab>();
//		UITabSet rootTabSet = tab.getRootTabSet();
//		EntityDefinition rootEntity = uiOptions.getRootEntityDefinition(rootTabSet);
//		
//		for (UITab childTab : tab.getTabs()) {
//			boolean unassigned = uiOptions.isUnassigned(childTab, rootEntity);
//			if ( unassigned ) {
//				unassignedTabs.add(childTab);
//			}
//		}
//		List<SchemaTreeNode> unassignedTabNodes = fromTabsList(unassignedTabs, version, includeAttributes, labelLanguage);
//		result.addAll(unassignedTabNodes);
		
		List<UITab> nestedTabs = new ArrayList<UITab>();
		for (UITab childTab : tab.getTabs()) {
			List<NodeDefinition> nodes = uiOptions.getNodesPerTab(childTab, false);
			boolean toBeAdded = true;
			for (NodeDefinition nestedTabChildNode : nodes) {
				for (NodeDefinition childDefn : childDefns ) {
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
		Collection<SchemaTreeNode> tabNodes = createNodes(nestedTabs);
		result.addAll(tabNodes);

		return result;
	}
	
	protected List<SchemaTreeNode> createNodes(UITab parentTab, List<? extends NodeDefinition> nodes) {
		List<SchemaTreeNode> result = null;
		if ( nodes != null ) {
			result = new ArrayList<SchemaTreeNode>();
			for (NodeDefinition nodeDefn : nodes) {
				if ( includeNodePredicate == null || includeNodePredicate.evaluate(nodeDefn) ) {
					CollectSurvey survey = (CollectSurvey) nodeDefn.getSurvey();
					UIOptions uiOptions = survey.getUIOptions();
					UITab assignedTab = uiOptions.getAssignedTab(nodeDefn);
					if ( assignedTab == parentTab && ( version == null || version.isApplicable(nodeDefn) ) ) {
						SchemaNodeData data = new SchemaNodeData(nodeDefn, nodeDefn.getName(), false, false);
						SchemaTreeNode treeNode = (SchemaTreeNode) createNode(data, false);
						if ( treeNode != null ) {
							result.add(treeNode);
						}
					}
				}
			}
		}
		return result;
	}
	
	private SchemaTreeNode createNode(UITab tab) {
		SchemaNodeData data = new SchemaNodeData(tab, tab.getLabel(labelLanguage), false, false);
		return (SchemaTreeNode) createNode(data, false);
	}
	
	private List<SchemaTreeNode> createNodes(List<UITab> tabs) {
		List<SchemaTreeNode> result = null;
		if ( tabs != null ) {
			result = new ArrayList<SchemaTreeNode>();
			for (UITab tab : tabs) {
				SchemaTreeNode node = createNode(tab);
				if ( node != null ) {
					result.add(node);
				}
			}
		}
		return result;
	}

}
