/**
 * 
 */
package org.openforis.collect.web.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.xml.DOMConfigurator;
import org.openforis.collect.manager.DatabaseVersionManager;
import org.openforis.collect.manager.DatabaseVersionNotCompatibleException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author M. Togna
 * 
 */
public class InitServlet extends HttpServlet {

	private String getTimestamp() {
		return new Date().toString();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();

		System.out.println("============= Open Foris Collect Initialization =============");

		// String basePath = getServletContext().getRealPath("/");

		// TODO init path
		// FileUtil.init(basePath);
		
		checkDatabaseVersion();
		
		this.initLog4J();
		// initValiadtionMap();

		System.out.println("====================================================");
	}

	private void checkDatabaseVersion() {
		System.out.println("============= Check database version compatibility... =============");
		WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		DatabaseVersionManager databaseVersionManager = applicationContext.getBean(DatabaseVersionManager.class);
		try {
			databaseVersionManager.checkIsVersionCompatible();
			System.out.println("======== Database version is compatible with application version ========");
		} catch (DatabaseVersionNotCompatibleException e) {
			System.out.println("======== ERROR: database version not compatible =========");
			throw new RuntimeException(e);			
		}
	}

	/**
	 * @throws FactoryConfigurationError
	 */
	protected void initLog4J() throws FactoryConfigurationError {
		// Get data from web.xml
		String file = this.getInitParameter("log4j-init-file");
		String logdir = this.getInitParameter("log4j-log-dir");

		if (logdir == null) {
			// Use default location for OpenForis logs if not specified in web.xml
			logdir = "WEB-INF/logs";
		}

		// Get path where OpenForis is running
		String openForisDir = this.getServletContext().getRealPath("/");

		// Define location of logfiles
		File logsdir = new File(openForisDir, logdir);
		logsdir.mkdirs();

		System.out.println(this.getTimestamp() + " - OpenForis logs dir=" + logsdir.getAbsolutePath());

		// Get log4j configuration file
		File srcConfigFile = new File(openForisDir, file);

		// Convert
		File log4jConfigFile = new File(openForisDir, "WEB-INF/TMPfile.xml");
		this.convertLogFile(srcConfigFile, log4jConfigFile, logsdir);

		System.out.println(this.getTimestamp() + " - OpenForis log4j configuration=" + log4jConfigFile.getAbsolutePath());

		// Configure log4j
		DOMConfigurator.configure(log4jConfigFile.getAbsolutePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// nothing
		// super.doGet(req, resp);
	}

	private void convertLogFile(File srcConfig, File destConfig, File logDir) {
		// Step 1 read config file into memory
		String srcDoc = "not initialized";
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileInputStream is = new FileInputStream(srcConfig);

			byte[] buf = new byte[1024];
			int len;
			while ((len = is.read(buf)) > 0) {
				baos.write(buf, 0, len);
			}

			is.close();
			baos.close();
			srcDoc = new String(baos.toByteArray());
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// Step 2 ; substitute Patterns
		String destDoc = srcDoc.replaceAll("loggerdir", logDir.getAbsolutePath().replaceAll("\\\\", "/"));

		// Step 3 ; write back to file
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(destDoc.getBytes());
			FileOutputStream fos = new FileOutputStream(destConfig);
			byte[] buf = new byte[1024];
			int len;
			while ((len = bais.read(buf)) > 0) {
				fos.write(buf, 0, len);
			}
			fos.close();
			bais.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
