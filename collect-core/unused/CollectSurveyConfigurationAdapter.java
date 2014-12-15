package org.openforis.collect.persistence.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openforis.collect.model.ui.UIOptions;
import org.openforis.idm.metamodel.Configuration;
import org.openforis.idm.metamodel.xml.ConfigurationAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectSurveyConfigurationAdapter implements ConfigurationAdapter<Configuration> {
	
	private static final String FLEX_TAG_NAME = "flex";
	
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
	public Configuration unmarshal(Element elem) {
		try {
			Configuration configuration = null;
			String namespaceURI = elem.getNamespaceURI();
			if (UIOptions.UI_NAMESPACE_URI.equals(namespaceURI) ) {
				String nodeName = elem.getLocalName();
				JAXBContext jc;
				if ( FLEX_TAG_NAME.equals(nodeName) ) {
					jc = JAXBContext.newInstance(UIOptions.class);
				} else {
					throw new IllegalArgumentException("Unsupported tag: " + nodeName);
				}
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				configuration = (Configuration) unmarshaller.unmarshal(elem);
				((UIOptions) configuration).init();
			}
			return configuration;
		} catch (JAXBException e) {
			throw new RuntimeException("Unable to marshal survey configuration: "+ e.getMessage(), e);
		}
	}

	@Override
	public Element marshal(Configuration config) {
		try {
			JAXBContext jc = JAXBContext.newInstance(config.getClass());
			Marshaller marshaller = jc.createMarshaller();
			Document document = documentBuilder.newDocument();
			marshaller.marshal(config, document);
			Element documentElement = document.getDocumentElement();
			return documentElement;
		} catch (JAXBException e) {
			throw new RuntimeException("Unable to marshal survey configuration: "+ e.getMessage(), e);
		}
	}

}
