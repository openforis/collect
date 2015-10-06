package org.openforis.collect.designer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectDesignerOptions {

	private static final Properties OPTIONS_PROPERTIES;
	
	private static final String idmExpressionLanguageWikiUrl;

	static {
		OPTIONS_PROPERTIES = loadOptionsProperties();
		
		idmExpressionLanguageWikiUrl = OPTIONS_PROPERTIES.getProperty("wiki.idm_expression_language.url");
	}

	private static Properties loadOptionsProperties() {
		InputStream is = null;
		try {
			is = CollectDesignerOptions.class.getResourceAsStream("options.properties");
			Properties properties = new Properties();
			properties.load(is);
			return properties;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public static String getIdmExpressionLanguageWikiUrl() {
		return idmExpressionLanguageWikiUrl;
	}
}
