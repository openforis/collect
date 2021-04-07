package org.openforis.web.server;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.Configuration.ClassList;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import net.lingala.zip4j.ZipFile;

public abstract class JettyApplicationServer implements ApplicationServer {

	private static final Logger LOG = LogManager.getLogger(JettyApplicationServer.class);

	private static final String LOCALHOST_ADDRESS = "localhost";
	
	protected int port;
	protected JndiDataSourceConfiguration[] jndiDsConfigurations;

	private Server server;
	protected File webappsFolder;
	
	public JettyApplicationServer(int port, File webappsFolder, JndiDataSourceConfiguration... jndiDsConfigurations) {
		super();
		this.port = port;
		this.webappsFolder = webappsFolder;
		this.jndiDsConfigurations = jndiDsConfigurations;
	}

	@Override
	public void initialize() throws IOException {
		printClasspath();
	}

	private void printClasspath() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader)cl).getURLs();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < urls.length; i++) {
			URL url = urls[i];
        	sb.append(url.getFile());
        	if (i < urls.length - 1) {
        		sb.append(';');
        	}
		}
    	LOG.info("Classpath: " + sb.toString());
	}
	
	@Override
	public void start() throws Exception {
		if (portAvailable()) {
			server = new Server(port);
			server.setStopAtShutdown(true);
			
			registerWebapps();
			
			server.start();
		} else {
			String message = String.format("Port %d already in use", port);
			LOG.error(message);
			throw new RuntimeException(message);
		}
	}

	private void registerWebapps() throws IOException, Exception, NamingException {
		LOG.info("Using webapps folder: " + webappsFolder.getAbsolutePath());
		
		//Enable parsing of jndi-related parts of web.xml and jetty-env.xml
	    ClassList classlist = ClassList.setServerDefault(server);
		classlist.addAfter( 
				"org.eclipse.jetty.webapp.FragmentConfiguration",
				"org.eclipse.jetty.plus.webapp.EnvConfiguration", 
				"org.eclipse.jetty.plus.webapp.PlusConfiguration"
		);
		classlist.addBefore(
	            "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
	            "org.eclipse.jetty.annotations.AnnotationConfiguration");
		
		File[] webappsFiles = webappsFolder.listFiles();
		
		if (webappsFiles == null || webappsFiles.length == 0) {
			throw new IllegalStateException(String.format("Empty webapps folder: %s", webappsFolder.getAbsolutePath()));
		}
		int webappsCount = 0;
		HandlerCollection handlerCollection = new HandlerCollection();
		ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
		for (File webappFile : webappsFiles) {
			if (webappFile.isFile() && webappFile.getName().toLowerCase().endsWith("war")) {
				WebAppContext webapp = createWebapp(webappFile);
				for (JndiDataSourceConfiguration jndiDsConfiguration : jndiDsConfigurations) {
					registerDbJndiResource(webapp, jndiDsConfiguration);
				}
				contextHandlerCollection.addHandler(webapp);
				LOG.info(String.format("Webapp %s registered", webapp.getContextPath()));
				webappsCount ++;
			}
		}
		handlerCollection.addHandler(contextHandlerCollection);
		server.setHandler(handlerCollection);
		
		LOG.info(String.format("%d webapps registered", webappsCount));
	}

	@Override
	public void stop() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

	@Override
	public boolean isRunning() {
		return server != null && server.isRunning();
	}

	@Override
	public String getUrl() {
		return String.format("%s://%s:%d/%s", "http", LOCALHOST_ADDRESS, port, getMainWebAppName());
	}
	
	private WebAppContext createWebapp(File warFile) {
		WebAppContext webapp = new WebAppContext();
		
		webapp.setConfigurations(new Configuration[] {
	            new EnvConfiguration(),
	            new FragmentConfiguration(), 
	            new JettyWebXmlConfiguration(),
	            new MetaInfConfiguration(),
	            new PlusConfiguration(),
	            new WebInfConfiguration(),
	            new WebXmlConfiguration(),
        });

		webapp.setParentLoaderPriority(true);

		String warFileName = warFile.getName();
		String context = warFileName.substring(0, warFileName.lastIndexOf("."));

		webapp.setContextPath("/" + context);
		
		File webappFolder = new File(warFile.getParentFile(), context);
		
		//unzip war file (if unzipped folder does not exist)
		if (! webappFolder.exists()) {
			try {
				new ZipFile(warFile).extractAll(webappFolder.getAbsolutePath());
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		webapp.setExtractWAR(false);
		webapp.setWar(webappFolder.getAbsolutePath());
		
		return webapp;
	}
	
	private boolean portAvailable() {
	    try (Socket ignored = new Socket(LOCALHOST_ADDRESS, port)) {
	        return false;
	    } catch (IOException ignored) {
	        return true;
	    }
	}

	private void registerDbJndiResource(WebAppContext context, JndiDataSourceConfiguration jndiDataSourceConfig) throws IOException, Exception, NamingException {
		Properties properties = jndiDataSourceConfig.toProperties();
		DataSource dataSource = BasicDataSourceFactory.createDataSource(properties);
		new Resource(context.getServer(), jndiDataSourceConfig.getJndiName(), dataSource);
		LOG.info(String.format("JNDI resource %s registered for webapp %s", 
				jndiDataSourceConfig.getJndiName(), context.getContextPath()));
	}

	protected abstract String getMainWebAppName();

}