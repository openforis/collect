package org.openforis.collect.controlpanel;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

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
import org.eclipse.jetty.util.RolloverFileOutputStream;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.Configuration.ClassList;
import org.eclipse.jetty.webapp.WebAppContext;
import org.openforis.web.server.ApplicationServer;
import org.openforis.web.server.JndiDataSourceConfiguration;

import net.lingala.zip4j.core.ZipFile;

public abstract class JettyApplicationServer implements ApplicationServer {

	protected int port;
	protected JndiDataSourceConfiguration[] jndiDsConfigurations;

	private Server server;
	protected File webappsFolder;
	private Logger logger;
	private File logFile;
	
	public JettyApplicationServer(int port, File webappsFolder, File logFile, JndiDataSourceConfiguration... jndiDsConfigurations) {
		super();
		this.port = port;
		this.webappsFolder = webappsFolder;
		this.jndiDsConfigurations = jndiDsConfigurations;
		this.logFile = logFile;
		this.logger = Log.getLogger(this.getClass());
	}

	@Override
	public void initialize() throws IOException {
		initializeLogger();
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
    	logger.info("Classpath: " + sb.toString());
	}
	
	@Override
	public void start() throws Exception {
		if (portAvailable()) {
			server = new Server(port);
			
			registerWebapps();
			
			server.start();
		} else {
			throw new RuntimeException(String.format("Port %d already in use", port));
		}
	}

	private void initializeLogger() throws IOException {
		RolloverFileOutputStream os = new RolloverFileOutputStream(this.logFile.getAbsolutePath(), false, 90,
				TimeZone.getTimeZone("GMT"), "yyyy_MM_dd", "yyyy_MM_dd_hhmm");
		
		PrintStream logStream = new PrintStream(os);

		System.setOut(logStream);
		System.setErr(logStream);
	}

	private void registerWebapps() throws IOException, Exception, NamingException {
		logger.info("Using webapps folder: " + webappsFolder.getAbsolutePath());
		
		//Enable parsing of jndi-related parts of web.xml and jetty-env.xml
	    ClassList classlist = ClassList.setServerDefault(server);
		classlist.addAfter(
				"org.eclipse.jetty.webapp.FragmentConfiguration",
				"org.eclipse.jetty.plus.webapp.EnvConfiguration", 
				"org.eclipse.jetty.plus.webapp.PlusConfiguration"
		);
		
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
				logger.info(String.format("Webapp %s registered", webapp.getContextPath()));
				webappsCount ++;
			}
		}
		handlerCollection.addHandler(contextHandlerCollection);
		server.setHandler(handlerCollection);
		
		logger.info(String.format("%d webapps registered", webappsCount));
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
	public File getLogFile() {
		return logFile;
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
	    try (Socket ignored = new Socket("localhost", port)) {
	        return false;
	    } catch (IOException ignored) {
	        return true;
	    }
	}

	private List<ContainerInitializer> jspInitializers() {
		JettyJasperInitializer sci = new JettyJasperInitializer();
		ContainerInitializer initializer = new ContainerInitializer(sci, null);
		return Arrays.asList(initializer);
	}

	private void registerDbJndiResource(WebAppContext context, JndiDataSourceConfiguration jndiDataSourceConfig) throws IOException, Exception, NamingException {
		Properties properties = jndiDataSourceConfig.toProperties();
		DataSource dataSource = BasicDataSourceFactory.createDataSource(properties);
		new Resource(context.getServer(), jndiDataSourceConfig.getJndiName(), dataSource);
		logger.info(String.format("JNDI resource %s registered for webapp %s", 
				jndiDataSourceConfig.getJndiName(), context.getContextPath()));
	}

	protected abstract String getMainWebAppName();

}