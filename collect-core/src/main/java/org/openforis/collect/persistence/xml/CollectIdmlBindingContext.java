package org.openforis.collect.persistence.xml;

import org.openforis.collect.model.UIConfiguration.UIConfigurationAdapter;
import org.openforis.idm.metamodel.Configuration;
import org.openforis.idm.metamodel.xml.ConfigurationAdapter;
import org.openforis.idm.metamodel.xml.IdmlBindingContext;

/**
 * @author G. Miceli
 */
public class CollectIdmlBindingContext extends IdmlBindingContext {
	public CollectIdmlBindingContext() {
		super();
		UIConfigurationAdapter configurationAdapter = new UIConfigurationAdapter();
		super.setConfigurationAdapter(configurationAdapter);
	}

	@Override
	public void setConfigurationAdapter(ConfigurationAdapter<? extends Configuration> configurationAdapter) {
		throw new UnsupportedOperationException();
	}
}
