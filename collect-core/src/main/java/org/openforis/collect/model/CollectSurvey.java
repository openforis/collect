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

	private boolean published;
	
	public CollectSurvey() {
		super();
	}
	
	public UIConfiguration getUIConfiguration() {
		List<Configuration> configurations = getConfiguration();
		for (Configuration config : configurations) {
			if ( config instanceof UIConfiguration ) {
				UIConfiguration uiConfig = (UIConfiguration) config;
				return uiConfig;
			}
		}
		return null;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}


}
