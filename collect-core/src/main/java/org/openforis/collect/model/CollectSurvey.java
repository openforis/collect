/**
 * 
 */
package org.openforis.collect.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.openforis.collect.model.ui.UIConfiguration;
import org.openforis.idm.metamodel.Configuration;
import org.openforis.idm.metamodel.Survey;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
@XmlRootElement(name = "survey")
public class CollectSurvey extends Survey {
	private static final long serialVersionUID = 1L;

	public CollectSurvey() {
		super();
	}
	
	public UIConfiguration getUIConfiguration() {
		List<Configuration> configurations = getConfiguration();
		for (Configuration config : configurations) {
			if ( config instanceof UIConfiguration ) {
				return (UIConfiguration) config;
			}
		}
		return null;
	}

	public void setUIConfiguration(UIConfiguration conf) {
		UIConfiguration oldConf = getUIConfiguration();
		if ( oldConf == null ) {
			removeConfiguration(oldConf);
		}
		addConfiguration(conf);
	}
	
	public LanguageConfiguration getLanguageConfiguration() {
		List<Configuration> configurations = getConfiguration();
		for (Configuration config : configurations) {
			if ( config instanceof LanguageConfiguration ) {
				return (LanguageConfiguration) config;
			}
		}
		return null;
	}
	
	public void setLanguageConfiguration(LanguageConfiguration conf) {
		LanguageConfiguration oldConf = getLanguageConfiguration();
		if ( oldConf != null ) {
			removeConfiguration(oldConf);
		}
		addConfiguration(conf);
	}
	
}
