package org.openforis.collect.controlpanel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

public class CollectServer {

	private static final String DB_PROPERTIES_FILE_NAME = "collect_db.properties";

	private static final String DB_JNDI_RESOURCE_NAME = "jdbc/collectDs";
	
	private int port;
	private WebAppConfiguration[] webAppConfigurations;
	
	private Server server;

	public CollectServer(int port, String context, WebAppConfiguration... webAppConfigurations) {
		super();
		this.port = port;
		this.webAppConfigurations = webAppConfigurations;
	}

	public void start() throws Exception {
		server = new Server(port);

		//Enable parsing of jndi-related parts of web.xml and jetty-env.xml
        org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(server);
		classlist.addAfter(
				"org.eclipse.jetty.webapp.FragmentConfiguration",
				"org.eclipse.jetty.plus.webapp.EnvConfiguration", 
				"org.eclipse.jetty.plus.webapp.PlusConfiguration"
		);
	

		HandlerCollection handlerCollection = new HandlerCollection();
		ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
		for (WebAppConfiguration webAppConfiguration : webAppConfigurations) {
			WebAppContext webapp = createWebapp(webAppConfiguration);
			registerDbJndiResource(webapp);
			contextHandlerCollection.addHandler(webapp);
		}
		handlerCollection.addHandler(contextHandlerCollection);
		server.setHandler(handlerCollection);
		
		server.start();
		
		server.join();
	}

	private WebAppContext createWebapp(WebAppConfiguration webAppConfiguration) {
		WebAppContext webapp = new WebAppContext();
		webapp.setConfigurationClasses(new String[]{
			"org.eclipse.jetty.webapp.WebInfConfiguration",
		    "org.eclipse.jetty.webapp.WebXmlConfiguration",
		    "org.eclipse.jetty.webapp.MetaInfConfiguration",
		    "org.eclipse.jetty.webapp.FragmentConfiguration",
		    "org.eclipse.jetty.plus.webapp.EnvConfiguration",
		    "org.eclipse.jetty.plus.webapp.PlusConfiguration",
		    "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
		});
		
		webapp.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
		webapp.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
		
		webapp.setParentLoaderPriority(true);
		
		webapp.setContextPath("/" + webAppConfiguration.getContext());
		webapp.setWar(webAppConfiguration.getWarFileLocation());
		return webapp;
	}
	
	private static List<ContainerInitializer> jspInitializers() {
		JettyJasperInitializer sci = new JettyJasperInitializer();
		ContainerInitializer initializer = new ContainerInitializer(sci, null);
		return Arrays.asList(initializer);
	}

	private void registerDbJndiResource(WebAppContext context) throws IOException, Exception, NamingException {
		Properties dbProp = new Properties();
		InputStream dbPropertiesIs = getClass().getClassLoader().getResourceAsStream(DB_PROPERTIES_FILE_NAME);
		dbProp.load(dbPropertiesIs);
		
		//replace system properties in url
		String url = dbProp.getProperty("url");
		String[] systemProps = {"user.home"};
		for (String propName : systemProps) {
			String propVal = System.getProperty(propName);
			url = url.replace("${" + propName + "}", propVal);
		}
		dbProp.setProperty("url", url);
		
		DataSource dataSource = BasicDataSourceFactory.createDataSource(dbProp);
		
		new Resource(context.getServer(), DB_JNDI_RESOURCE_NAME, dataSource);
	}

	public void stop() throws Exception {
		server.stop();
	}
	
	public static class WebAppConfiguration {
		
		private String warFileLocation;
		private String context;
		
		public WebAppConfiguration(String warFileLocation, String context) {
			super();
			this.warFileLocation = warFileLocation;
			this.context = context;
		}
		
		public String getWarFileLocation() {
			return warFileLocation;
		}
		
		public String getContext() {
			return context;
		}
	}
}
