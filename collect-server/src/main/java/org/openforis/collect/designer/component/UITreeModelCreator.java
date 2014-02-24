package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openforis.collect.designer.component.BasicTreeModel.AbstractNode;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaNodeData;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaTreeNode;
import org.openforis.collect.designer.util.Predicate;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
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

	public UITreeModelCreator(ModelVersion version,
			Predicate<SurveyObject> includeNodePredicate,
			boolean includeEmptyNodes, String labelLanguage) {
		super(version, includeNodePredicate, includeEmptyNodes, labelLanguage);
	}
	
	@Override
	protected List<AbstractNode<SchemaNodeData>> createChildNodes(SurveyObject surveyObject) {
		List<AbstractNode<SchemaNodeData>> childNodes = new ArrayList<AbstractNode<SchemaNodeData>>();
		if ( surveyObject instanceof EntityDefinition ) {
			List<AbstractNode<SchemaNodeData>> entityChildrenNodes = createChildNodes((EntityDefinition) surveyObject);
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

	private List<AbstractNode<SchemaNodeData>> createChildNodes(EntityDefinition entityDefn) {
		List<AbstractNode<SchemaNodeData>> childNodes = new ArrayList<AbstractNode<SchemaNodeData>>();
		CollectSurvey survey = (CollectSurvey) entityDefn.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		UITab assignedTab = uiOptions.getAssignedTab(entityDefn);
		
		List<NodeDefinition> childDefns = entityDefn.getChildDefinitions();
		Collection<? extends AbstractNode<SchemaNodeData>> schemaTreeNodes = createNodes(assignedTab, childDefns);
		childNodes.addAll(schemaTreeNodes);
		
		//include tabs
		List<UITab> tabs = uiOptions.getTabsAssignableToChildren(entityDefn);
		Collection<? extends AbstractNode<SchemaNodeData>> tabNodes = createNodes(tabs);
		childNodes.addAll(tabNodes);
		return childNodes;
	}

	@Override
	protected List<AbstractNode<SchemaNodeData>> createFirstLevelNodes(EntityDefinition rootEntity) {
		List<AbstractNode<SchemaNodeData>> firstLevelTreeNodes = new ArrayList<AbstractNode<SchemaNodeData>>();
		CollectSurvey survey = (CollectSurvey) rootEntity.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		UITabSet tabSet = uiOptions.getAssignedRootTabSet(rootEntity);
		for (UITab tab : tabSet.getTabs()) {
			SchemaTreeNode node = createNode(tab);
			if ( node != null ) {
				firstLevelTreeNodes.add(node);
			}
		}
		return firstLevelTreeNodes;
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
//		List<UITab> tabs = tab.getTabs();
//		for (UITab childTab : tabs) {
//			UITabSet rootTabSet = childTab.getRootTabSet();
//			EntityDefinition parentEntity = uiOptions.getRootEntityDefinition(rootTabSet);
//			boolean unassigned = uiOptions.isUnassigned(childTab, parentEntity);
//			if ( unassigned ) {
//				unassignedTabs.add(childTab);
//			}
//		}
//		List<SchemaTreeNode> unassignedTabNodes = fromTabsList(unassignedTabs, version, includeAttributes, labelLanguage);
//		result.addAll(unassignedTabNodes);
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
