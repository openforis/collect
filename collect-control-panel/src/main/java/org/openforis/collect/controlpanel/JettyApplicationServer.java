package org.openforis.collect.controlpanel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.eclipse.jetty.webapp.Configuration.ClassList;
import org.eclipse.jetty.webapp.WebAppContext;
import org.openforis.web.server.ApplicationServer;
import org.openforis.web.server.JndiDataSourceConfiguration;

public abstract class JettyApplicationServer implements ApplicationServer {

	protected int port;
	protected JndiDataSourceConfiguration[] jndiDsConfigurations;

	private Server server;
	protected Path log;
	protected File webappsFolder;
	
	public JettyApplicationServer(int port, File webappsFolder, JndiDataSourceConfiguration... jndiDsConfigurations) {
		super();
		this.port = port;
		this.webappsFolder = webappsFolder;
		this.jndiDsConfigurations = jndiDsConfigurations;
		this.log = Paths.get(getDefaultLogFileLocation());
	}

	@Override
	public void start() throws Exception {
		server = new Server(port);
	
		//Enable parsing of jndi-related parts of web.xml and jetty-env.xml
	    ClassList classlist = ClassList.setServerDefault(server);
		classlist.addAfter(
				"org.eclipse.jetty.webapp.FragmentConfiguration",
				"org.eclipse.jetty.plus.webapp.EnvConfiguration", 
				"org.eclipse.jetty.plus.webapp.PlusConfiguration"
		);
		//add webapps
		File[] webappsFiles = webappsFolder.listFiles();
		HandlerCollection handlerCollection = new HandlerCollection();
		ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
		for (File webappFile : webappsFiles) {
			if (webappFile.isFile() && webappFile.getName().toLowerCase().endsWith("war")) {
				WebAppContext webapp = createWebapp(webappFile);
				for (JndiDataSourceConfiguration jndiDsConfiguration : jndiDsConfigurations) {
					registerDbJndiResource(webapp, jndiDsConfiguration);
				}
				contextHandlerCollection.addHandler(webapp);
			}
		}
		handlerCollection.addHandler(contextHandlerCollection);
		server.setHandler(handlerCollection);
		
		server.start();
	}

	@Override
	public void stop() throws Exception {
		server.stop();
	}

	@Override
	public boolean isRunning() {
		return server.isRunning();
	}

	@Override
	public String getUrl() {
		return String.format("%s://%s:%d/%s", "http", "localhost", port, getMainWebAppName());
	}

	@Override
	public Path getLog() {
		return log;
	}

	private WebAppContext createWebapp(File warFile) {
		WebAppContext webapp = new WebAppContext();
		webapp.setConfigurationClasses(new String[]{
			"org.eclipse.jetty.webapp.WebInfConfiguration",
		    "org.eclipse.jetty.webapp.WebXmlConfiguration",
		    "org.eclipse.jetty.webapp.MetaInfConfiguration",
		    "org.eclipse.jetty.webapp.FragmentConfiguration",
		    "org.eclipse.jetty.plus.webapp.EnvConfiguration",
		    "org.eclipse.jetty.plus.webapp.PlusConfiguration",
		    "org.eclipse.jetty.webapp.JettyWebXmlConfiguration"
		});
		
		webapp.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
		webapp.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
		
		webapp.setParentLoaderPriority(true);
		
		String warFileName = warFile.getName();
		String context = warFileName.substring(0, warFileName.lastIndexOf("."));
		webapp.setContextPath("/" + context);
		webapp.setWar(warFile.getAbsolutePath());
		return webapp;
	}
	
	private static List<ContainerInitializer> jspInitializers() {
		JettyJasperInitializer sci = new JettyJasperInitializer();
		ContainerInitializer initializer = new ContainerInitializer(sci, null);
		return Arrays.asList(initializer);
	}

	private void registerDbJndiResource(WebAppContext context, JndiDataSourceConfiguration jndiDataSourceConfig) throws IOException, Exception, NamingException {
		Properties properties = jndiDataSourceConfig.toProperties();
		DataSource dataSource = BasicDataSourceFactory.createDataSource(properties);
		new Resource(context.getServer(), jndiDataSourceConfig.getJndiName(), dataSource);
	}

	protected abstract String getMainWebAppName();

	protected abstract String getDefaultLogFileLocation();

}