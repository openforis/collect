package org.openforis.collect.metamodel.proxy;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="", propOrder={"tabs"}, namespace = "http://www.openforis.org/collect/3.0/ui")
@XmlRootElement(name = "flex", namespace = "http://www.openforis.org/collect/3.0/ui")
public class UIConfiguration implements ProxyBase {
	
	@XmlElementWrapper(name = "tabs", namespace = "http://www.openforis.org/collect/3.0/ui")
	@XmlElement(name = "tab", type = UITab.class, namespace = "http://www.openforis.org/collect/3.0/ui")
	private List<UITab> tabs;
	
	public List<UITab> getTabs() {
		return tabs;
	}
	
	public static UIConfiguration unmarshal(Element el) throws IOException {
		try {
			JAXBContext jc = JAXBContext.newInstance(UIConfiguration.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			UIConfiguration configuration = (UIConfiguration) unmarshaller.unmarshal(el);
			return configuration;
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	
}
