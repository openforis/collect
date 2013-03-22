package org.openforis.collect.remoting.service;

import java.io.File;

import javax.servlet.ServletContext;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.manager.referencedataimport.proxy.ReferenceDataImportStatusProxy;
import org.openforis.collect.util.ExecutorServiceUtil;
import org.openforis.collect.web.controller.FileUploadController;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class ReferenceDataImportService<S extends ReferenceDataImportStatusProxy, P extends AbstractProcess<Void, ?>> {
	
	@Autowired 
	private ServletContext servletContext;
	@Autowired
	private SessionManager sessionManager;
	
	private String importFileName;
	private File tempDirectory;
	protected P importProcess;
	
	public ReferenceDataImportService(String importFileName) {
		super();
		this.importFileName = importFileName;
	}

	protected void init() {
		String tempRealPath = servletContext.getRealPath(FileUploadController.TEMP_PATH);
		tempDirectory = new File(tempRealPath);
		if ( tempDirectory.exists() ) {
			tempDirectory.delete();
		}
		if ( ! tempDirectory.mkdirs() && ! tempDirectory.canRead() ) {
			throw new IllegalStateException("Cannot access import directory: " + tempRealPath);
		}
	}
	
	protected File getImportFile() {
		SessionState sessionState = sessionManager.getSessionState();
		File userImportFolder = FileUploadController.getSessionTempDirectory(tempDirectory, sessionState.getSessionId());
		File importFile = new File(userImportFolder, importFileName);
		return importFile;
	}
	
	protected void startProcessThread() {
		ExecutorServiceUtil.executeInCachedPool(importProcess);
	}

	@Secured("ROLE_ADMIN")
	public void cancel() {
		if ( importProcess != null ) {
			importProcess.cancel();
		}
	}
	
	@Secured("ROLE_ADMIN")
	public abstract S getStatus();
	
}
