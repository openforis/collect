package org.openforis.collect.model;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openforis.idm.metamodel.Configuration;
import org.openforis.idm.metamodel.xml.ConfigurationAdapter;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "tabs" }, namespace = "http://www.openforis.org/collect/3.0/ui")
@XmlRootElement(name = "flex", namespace = "http://www.openforis.org/collect/3.0/ui")

public class UIConfiguration implements Configuration {

	@XmlElementWrapper(name = "tabs", namespace = "http://www.openforis.org/collect/3.0/ui")
	@XmlElement(name = "tab", type = UITab.class, namespace = "http://www.openforis.org/collect/3.0/ui")
	private List<UITab> tabs;

	public List<UITab> getTabs() {
		return tabs;
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
				throw new RuntimeException("Unable to unmarshal the UI configuration", e);
			}
		}

		@Override
		public Element marshal(UIConfiguration config) {
			try {
				JAXBContext jc = JAXBContext.newInstance(UIConfiguration.class);
				Marshaller marshaller = jc.createMarshaller();
				DOMImplementation domImplementation = documentBuilder.getDOMImplementation();
				Document document = domImplementation.createDocument("http://www.openforis.org/collect/3.0/ui", "ui:flex", null);
//				Document document = documentBuilder.newDocument();
//				document.setPrefix("ui");
				//document.set
				marshaller.marshal(config, document);
				Element documentElement = document.getDocumentElement();
				return documentElement;
			} catch (JAXBException e) {
				throw new RuntimeException("Unable to marshal the UI configuration", e);
			}
		}

	}

}
