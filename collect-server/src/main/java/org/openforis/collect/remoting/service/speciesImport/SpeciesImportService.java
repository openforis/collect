package org.openforis.collect.remoting.service.speciesImport;

import java.io.File;

import javax.servlet.ServletContext;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.speciesImport.SpeciesImportProcess;
import org.openforis.collect.manager.speciesImport.SpeciesImportStatus;
import org.openforis.collect.remoting.service.dataImport.DataImportExeption;
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
public class SpeciesImportService {
	
	private static final String FILE_NAME = "species.csv";
	
	@Autowired
	private SessionManager sessionManager;
	
	@Autowired
	private SpeciesManager speciesManager;
	
	@Autowired 
	private ServletContext servletContext;
	
	private File tempDirectory;
	
	private SpeciesImportProcess importProcess;

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
	
	@Secured("ROLE_ADMIN")
	public SpeciesImportStatus start(String taxonomyName, boolean overwriteAll) throws DataImportExeption {
		if ( importProcess == null || ! importProcess.getStatus().isRunning() ) {
			SessionState sessionState = sessionManager.getSessionState();
			File userImportFolder = FileUploadController.getSessionTempDirectory(tempDirectory, sessionState.getSessionId());
			File importFile = new File(userImportFolder, FILE_NAME);
			importProcess = new SpeciesImportProcess(speciesManager, taxonomyName, importFile);
			ExecutorServiceUtil.executeInCachedPool(importProcess);
		}
		SpeciesImportStatus status = importProcess.getStatus();
		return status;
	}
	
	@Secured("ROLE_ADMIN")
	public SpeciesImportStatus getStatus() {
		if ( importProcess != null ) {
			SpeciesImportStatus status = importProcess.getStatus();
			return status;
		} else {
			return null;
		}
	}
	
	@Secured("ROLE_ADMIN")
	public void cancel() {
		if ( importProcess != null ) {
			importProcess.cancel();
		}
	}
	
}
