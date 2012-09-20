/**
 * 
 */
package org.openforis.collect.designer.composer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.ui.UIConfiguration;
import org.openforis.collect.model.ui.UITab;
import org.openforis.collect.model.ui.UITabDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 * @author S. Ricci
 *
 */
public class TabsGroupPanelComposer extends BindComposer<Component> {

	private static final long serialVersionUID = 1L;
	
	private UITab tab;
	
	@WireVariable
	private Session _sess;
	
	@Init
	public void init(@ExecutionArgParam("tab") UITab tab) {
		this.tab = tab;
	}
	
	public UITab getTab() {
		return tab;
	}
	
	public List<NodeDefinition> getNodesPerTab() {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		CollectSurvey survey = getSurvey();
		UIConfiguration uiConf = survey.getUIConfiguration();
		uiConf.setSurvey(survey);
		UITabDefinition tabDefinition = tab.getTabDefinition();
		EntityDefinition rootEntity = uiConf.getRootEntityDefinition(tabDefinition);
		Queue<NodeDefinition> queue = new LinkedList<NodeDefinition>();
		queue.addAll(rootEntity.getChildDefinitions());
		while ( ! queue.isEmpty() ) {
			NodeDefinition defn = queue.remove();
			UITab nodeTab = uiConf.getTab(defn);
			if ( nodeTab != null && nodeTab.equals(tab) ) {
				result.add(defn);
			}
			if ( defn instanceof EntityDefinition ) {
				queue.addAll(((EntityDefinition) defn).getChildDefinitions());
			}
		}
		return result;
	}

	private CollectSurvey getSurvey() {
		SessionStatus sessionStatus = (SessionStatus) _sess.getAttribute(SessionStatus.SESSION_KEY);
		CollectSurvey survey = sessionStatus.getSurvey();
		return survey;
	}
	
}
