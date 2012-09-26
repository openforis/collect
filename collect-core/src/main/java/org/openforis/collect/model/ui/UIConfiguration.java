package org.openforis.collect.model.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.Configuration;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.ConfigurationAdapter;
import org.openforis.idm.util.CollectionUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "tabDefinitions" })
@XmlRootElement(name = "flex")
public class UIConfiguration implements Configuration, Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final QName TAB_DEFINITION_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/ui", "tabDefinition");
	public static final QName TAB_NAME_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/ui", "tab");
	public static final QName LAYOUT_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/ui", "layout");
	
	public enum Layout {
		FORM, TABLE
	}
	
	@XmlElement(name = "tabDefinition", type = UITabDefinition.class)
	private List<UITabDefinition> tabDefinitions;

	private transient CollectSurvey survey;

	public void init() {
		initParentRefernces();
	}

	protected void initParentRefernces() {
		if ( tabDefinitions != null ) {
			for (UITabsGroup group : tabDefinitions) {
				setParentInChildrenTabs(group);
			}
		}
	}

	protected void setParentInChildrenTabs(UITabsGroup group) {
		List<UITab> tabs = group.getTabs();
		for (UITab uiTab : tabs) {
			uiTab.setParent(group);
			setParentInChildrenTabs(uiTab);
		}
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public List<UITabDefinition> getTabDefinitions() {
		return CollectionUtil.unmodifiableList(tabDefinitions);
	}
	
	public UITab getTab(NodeDefinition nodeDefn) {
		return getTab(nodeDefn, true);
	}
	
	public UITab getTab(NodeDefinition nodeDefn, boolean includeInherited) {
		UITab tab = null;
		EntityDefinition rootEntity = nodeDefn.getRootEntity();
		UITabDefinition tabDefinition = getTabDefinition(rootEntity);
		if ( tabDefinition != null ) {
			String tabName = nodeDefn.getAnnotation(TAB_NAME_ANNOTATION);
			NodeDefinition parentDefn = nodeDefn.getParentDefinition();
			if ( StringUtils.isNotBlank(tabName) && ( parentDefn == null || parentDefn.getParentDefinition() == null ) ) {
				tab = tabDefinition.getTab(tabName);
			} else if ( parentDefn != null ) {
				UITab parentTab = getTab(parentDefn);
				if ( parentTab != null && StringUtils.isNotBlank(tabName) ) {
					tab = parentTab.getTab(tabName);
				} else if ( includeInherited ) {
					tab = parentTab;
				}
			}
		}
		return tab;
	}

	public UITabDefinition getTabDefinition(EntityDefinition rootEntityDefn) {
		String tabDefnName = rootEntityDefn.getAnnotation(TAB_DEFINITION_ANNOTATION);
		UITabDefinition tabDefinition = getTabDefinition(tabDefnName);
		return tabDefinition;
	}
	
	public List<UITab> getAllowedTabs(NodeDefinition nodeDefn) {
		EntityDefinition rootEntity = nodeDefn.getRootEntity();
		UITabDefinition tabDefinition = getTabDefinition(rootEntity);
		if ( tabDefinition != null ) {
			NodeDefinition parentDefn = nodeDefn.getParentDefinition();
			if ( parentDefn == null || parentDefn.getParentDefinition() == null ) {
				return tabDefinition.getTabs();
			} else {
				UITab parentTab = getTab(parentDefn);
				if ( parentTab != null ) {
					return parentTab.getTabs();
				}
			}
		}
		return Collections.emptyList();
	}

	public boolean isAssignableTo(NodeDefinition nodeDefn, UITab tab) {
		List<UITab> allowedTabs = getAllowedTabs(nodeDefn);
		boolean result = allowedTabs.contains(tab);
		return result;
	}

	public List<NodeDefinition> getNodesPerTab(UITab tab, boolean includeChildrenOfEntities) {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		UITabDefinition tabDefinition = tab.getTabDefinition();
		EntityDefinition rootEntity = getRootEntityDefinition(tabDefinition);
		Queue<NodeDefinition> queue = new LinkedList<NodeDefinition>();
		queue.addAll(rootEntity.getChildDefinitions());
		while ( ! queue.isEmpty() ) {
			NodeDefinition defn = queue.remove();
			UITab nodeTab = getTab(defn);
			boolean nodeInTab = false;
			if ( nodeTab != null && nodeTab.equals(tab) ) {
				result.add(defn);
				nodeInTab = true;
			}
			if ( defn instanceof EntityDefinition && (includeChildrenOfEntities || !nodeInTab) ) {
				queue.addAll(((EntityDefinition) defn).getChildDefinitions());
			}
		}
		return result;
	}

	public void associateWithTab(NodeDefinition nodeDefn, UITab tab) {
		String tabName = tab.getName();
		nodeDefn.setAnnotation(TAB_NAME_ANNOTATION, tabName);
	}
	
	public void removeTabAssociation(NodeDefinition nodeDefn) {
		nodeDefn.setAnnotation(TAB_NAME_ANNOTATION, null);
	}
	
	public Layout getLayout(EntityDefinition node) {
		String layoutValue = node.getAnnotation(LAYOUT_ANNOTATION);
		if ( layoutValue == null ) {
			EntityDefinition parent = (EntityDefinition) node.getParentDefinition();
			if ( parent != null ) {
				if ( node.isMultiple()) {
					return Layout.TABLE;
				} else {
					return getLayout(parent);
				}
			} else {
				return Layout.FORM;
			}
		}
		return layoutValue != null ? Layout.valueOf(layoutValue.toUpperCase()): null;
	}

	public void setLayout(NodeDefinition nodeDefn, Layout layout) {
		String layoutValue = layout != null ? layout.name().toLowerCase(): null;
		nodeDefn.setAnnotation(LAYOUT_ANNOTATION, layoutValue);
	}

	public EntityDefinition getRootEntityDefinition(
			UITabDefinition tabDefinition) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		for (EntityDefinition defn : rootEntityDefinitions) {
			UITabDefinition entityTabDefn = getTabDefinition(defn);
			if ( entityTabDefn != null && entityTabDefn.equals(tabDefinition) ) {
				return defn;
			}
		}
		return null;
	}
	
	public UITabDefinition getTabDefinition(String name) {
		if ( tabDefinitions != null ) {
			for (UITabDefinition tabDefn : tabDefinitions) {
				if ( tabDefn.getName().equals(name) ) {
					return tabDefn;
				}
			}
		}
		return null;
	}
	
	public void addTabDefinition(UITabDefinition tabDefn) {
		if ( tabDefinitions == null ) {
			tabDefinitions = new ArrayList<UITabDefinition>();
		}
		tabDefinitions.add(tabDefn);
	}
	
	public void setTabDefinition(int index, UITabDefinition tabDefn) {
		if ( tabDefinitions == null ) {
			tabDefinitions = new ArrayList<UITabDefinition>();
		}
		tabDefinitions.set(index, tabDefn);
	}
	
	public void removeTabDefinition(UITabDefinition tabDefn) {
		tabDefinitions.remove(tabDefn);
	}
	
	public UITabDefinition updateTabDefinition(String name, String newName) {
		UITabDefinition tabDefn = getTabDefinition(name);
		tabDefn.setName(newName);
		return tabDefn;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tabDefinitions == null) ? 0 : tabDefinitions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UIConfiguration other = (UIConfiguration) obj;
		if (tabDefinitions == null) {
			if (other.tabDefinitions != null)
				return false;
		} else if (!tabDefinitions.equals(other.tabDefinitions))
			return false;
		return true;
	}

	public static class UIConfigurationAdapter implements ConfigurationAdapter<UIConfiguration> {

		private static DocumentBuilder documentBuilder;
		
		static{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			try {
				documentBuilder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public UIConfiguration unmarshal(Element elem) {
			try {
				JAXBContext jc = JAXBContext.newInstance(UIConfiguration.class);
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				UIConfiguration configuration = (UIConfiguration) unmarshaller.unmarshal(elem);
				configuration.init();
				return configuration;
			} catch (JAXBException e) {
				throw new RuntimeException("Unable to marshal the UI configuration: "+ e.getMessage(), e);
			}
		}

		@Override
		public Element marshal(UIConfiguration config) {
			try {
				JAXBContext jc = JAXBContext.newInstance(UIConfiguration.class);
				Marshaller marshaller = jc.createMarshaller();
				Document document = documentBuilder.newDocument();
				marshaller.marshal(config, document);
				Element documentElement = document.getDocumentElement();
				return documentElement;
			} catch (JAXBException e) {
				throw new RuntimeException("Unable to marshal the UI configuration: "+ e.getMessage(), e);
			}
		}

	}

}
