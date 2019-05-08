package org.openforis.collect.reporting;

import org.apache.commons.lang3.ObjectUtils;
import org.openforis.collect.manager.ConfigurationManager;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class SaikuConfiguration implements InitializingBean {

	private static final String DEFAULT_CONTEXT = "saiku";

	@Autowired
	private ConfigurationManager configurationManager;
	
	private String contextPath;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

	public void init() {
		this.contextPath = ObjectUtils.defaultIfNull(configurationManager.getConfiguration().get(ConfigurationItem.SAIKU_CONTEXT),
				DEFAULT_CONTEXT);
	}
	
	public String getContextPath() {
		return contextPath;
	}
	
}
