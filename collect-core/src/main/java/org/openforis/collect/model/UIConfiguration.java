package org.openforis.collect.model;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openforis.idm.metamodel.Configuration;
import org.openforis.idm.metamodel.xml.ConfigurationAdapter;
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
public class UIConfiguration implements Configuration {

	@XmlElement(name = "tabDefinition", type = UITabDefinition.class)
	private List<UITabDefinition> tabDefinitions;

	public List<UITabDefinition> getTabDefinitions() {
		return tabDefinitions;
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

		/*
		public static void main(String[] a) {
			Document xmldoc= new DocumentImpl();
			Element flex = xmldoc.createElementNS("http://www.openforis.org/collect/3.0/ui", "flex");
			Element tabDefinition = xmldoc.createElementNS("http://www.openforis.org/collect/3.0/ui", "tabDefinition");
			tabDefinition.setAttributeNS(null, "rootEntity", "myRootEntity");
			Element tabs = xmldoc.createElementNS("http://www.openforis.org/collect/3.0/ui", "tabs");
			Element tab = xmldoc.createElementNS("http://www.openforis.org/collect/3.0/ui", "tab");
			tab.setAttributeNS(null, "name", "tab1");
			Element label = xmldoc.createElementNS("http://www.openforis.org/collect/3.0/ui", "label");
			label.setTextContent("Tab 1 label");
			tab.appendChild(label);
			tabs.appendChild(tab);
			tabDefinition.appendChild(tabs);
			flex.appendChild(tabDefinition);
			UIConfiguration uiConfiguration = new UIConfigurationAdapter().unmarshal(flex);
			System.out.println(uiConfiguration.getTabDefinitions().size());
		}
		*/
	}

}
