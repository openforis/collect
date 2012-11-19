package org.openforis.collect.metamodel.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.validation.CollectValidator;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.path.InvalidPathException;

/**
 * 
 * @author S. Ricci
 *
 */
public class UIOptionsTest {

	private CollectSurvey survey;
	private Schema schema;
	private UIOptions uiOptions;

	@Before
	public void setUp() {
		initSurvey();
	}

	protected void initSurvey() {
		SurveyContext ctx = new CollectSurveyContext(new ExpressionFactory(), new CollectValidator(), null);
		survey = (CollectSurvey) ctx.createSurvey();
		schema = survey.getSchema();
		EntityDefinition cluster = schema.createEntityDefinition();
		cluster.setName("cluster");
		schema.addRootEntityDefinition(cluster);
		uiOptions = survey.createUIOptions();
		UITabSet clusterTabSet = uiOptions.createTabSet();
		clusterTabSet.setName("cluster");
		addInnerTabs(clusterTabSet, "cluster", "plot", "informant");
		UITab clusterTab = clusterTabSet.getTab("cluster");
		UITab plotTab = clusterTabSet.getTab("plot");
		addInnerTabs(plotTab, "plot_det", "shrubs_regen", "tree", "dead_wood", "stump", "bamboo");
		UITab treeTab = plotTab.getTab("tree");
		UITab plotDetTab = plotTab.getTab("plot_det");
		addInnerTabs(plotDetTab, "plot_det_1", "plot_det_2", "plot_det_3");
		uiOptions.addTabSet(clusterTabSet);
		UITabSet householdTabSet = uiOptions.createTabSet();
		householdTabSet.setName("household");
		addInnerTabs(householdTabSet, "hs_general", "hs_assets", "hs_foodsecurity");
		uiOptions.addTabSet(householdTabSet);
		survey.addApplicationOptions(uiOptions);
		
		EntityDefinition plot = schema.createEntityDefinition();
		plot.setName("plot");
		cluster.addChildDefinition(plot);
		EntityDefinition tree = schema.createEntityDefinition();
		tree.setName("tree");
		plot.addChildDefinition(tree);
		
		uiOptions.assignToTabSet(cluster, clusterTabSet);
		uiOptions.assignToTab(cluster, clusterTab);
		uiOptions.assignToTab(plot, plotTab);
		uiOptions.assignToTab(tree, treeTab);
	}
	
	protected void addInnerTabs(UITabSet tabSet, String... tabNames) {
		for (String name : tabNames) {
			UITab innerTab = uiOptions.createTab();
			innerTab.setName(name);
			tabSet.addTab(innerTab);
		}
	}
	
	@Test
	public void testUIOptionsGeneration() {
		EntityDefinition cluster = schema.getRootEntityDefinition("cluster");

		UITabSet clusterTabSet = uiOptions.getTabSet("cluster");
		assertEquals(0, clusterTabSet.getDepth());
		
		EntityDefinition plot = (EntityDefinition) cluster.getChildDefinition("plot");
		UITab plotTab = uiOptions.getAssignedTab(plot);
		assertEquals(1, plotTab.getDepth());
		
		EntityDefinition tree = (EntityDefinition) plot.getChildDefinition("tree");
		UITab treeTab = uiOptions.getAssignedTab(tree);
		assertEquals(2, treeTab.getDepth());
	}
	
	@Test
	public void testTabSetAssociation() throws InvalidPathException {
		UITabSet clusterTabSet = uiOptions.getTabSet("cluster");
		assertNotNull(clusterTabSet);
		assertEquals("cluster", clusterTabSet.getName());
		EntityDefinition cluster = schema.getRootEntityDefinition("cluster");
		clusterTabSet = uiOptions.getAssignedRootTabSet(cluster);
		assertEquals("cluster", clusterTabSet.getName());
		
		EntityDefinition plot = (EntityDefinition) cluster.getChildDefinition("plot");
		UITab assignedTab = uiOptions.getAssignedTab(plot);
		assertEquals("plot", assignedTab.getName());
		
		NodeDefinition tree = plot.getChildDefinition("tree");
		assignedTab = uiOptions.getAssignedTab(tree);
		assertEquals("tree", assignedTab.getName());
		
		UITabSet assignedToParentTabSet = uiOptions.getAssignedTabSet(plot);
		assertEquals("plot", assignedToParentTabSet.getName());
		
		assignedToParentTabSet = uiOptions.getAssignedTabSet(cluster);
		assertEquals("cluster", assignedToParentTabSet.getName());
	}
	
	@Test
	public void testAssignableTabs() throws InvalidPathException {
		EntityDefinition clusterDefn = schema.getRootEntityDefinition("cluster");
		EntityDefinition plotDefn = (EntityDefinition) clusterDefn.getChildDefinition("plot");
		List<UITab> plotAssignableTabs = uiOptions.getAssignableTabs(plotDefn);
		assertEquals(3, plotAssignableTabs.size());
		EntityDefinition treeDefn = (EntityDefinition) plotDefn.getChildDefinition("tree");
		List<UITab> treeAssignableTabs = uiOptions.getAssignableTabs(treeDefn);
		assertEquals(6, treeAssignableTabs.size());
	}

	@Test
	public void testAssignableTabsToChildren() throws InvalidPathException {
		EntityDefinition clusterDefn = schema.getRootEntityDefinition("cluster");
		List<UITab> clusterTabs = uiOptions.getTabsAssignableToChildren(clusterDefn);
		assertEquals(3, clusterTabs.size());
		UITab plotTab = clusterTabs.get(1);
		assertEquals("plot", plotTab.getName());
		EntityDefinition plotDefn = (EntityDefinition) clusterDefn.getChildDefinition("plot");
		List<UITab> plotTabs = uiOptions.getTabsAssignableToChildren(plotDefn);
		assertEquals(6, plotTabs.size());
		UITab treeTab = plotTabs.get(2);
		assertEquals("tree", treeTab.getName());
		EntityDefinition treeDefn = (EntityDefinition) plotDefn.getChildDefinition("tree");
		List<UITab> treeAssignableTabs = uiOptions.getAssignableTabs(treeDefn);
		assertEquals(6, treeAssignableTabs.size());
	}

	@Test
	public void testDetachedNodeAssignableTabs() throws InvalidPathException {
		EntityDefinition clusterDefn = schema.getRootEntityDefinition("cluster");
		EntityDefinition plotDefn = (EntityDefinition) clusterDefn.getChildDefinition("plot");
		List<UITab> assignableTabs = uiOptions.getTabsAssignableToChildren(plotDefn);
		assertEquals(6, assignableTabs.size());
		UITab treeTab = assignableTabs.get(2);
		assertEquals("tree", treeTab.getName());
	}
}
