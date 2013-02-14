package org.openforis.collect.remoting.service;

import java.io.File;

import javax.servlet.ServletContext;

import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.samplingDesignImport.SamplingDesignImportProcess;
import org.openforis.collect.manager.samplingDesignImport.SamplingDesignImportStatus;
import org.openforis.collect.remoting.service.dataImport.DataImportExeption;
import org.openforis.collect.remoting.service.samplingDesignImport.proxy.SamplingDesignImportStatusProxy;
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
public class SamplingDesignImportService {
	
	private static final String INTERNAL_ERROR_IMPORTING_FILE_MESSAGE_KEY = "samplingDesignImport.error.internalErrorImportingFile";

	private static final String FILE_NAME = "sampling_design.csv";
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired 
	private ServletContext servletContext;
	
	private File tempDirectory;
	private SamplingDesignImportProcess importProcess;

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
	public SamplingDesignImportStatusProxy start(int surveyId, boolean work, boolean overwriteAll) throws DataImportExeption {
		if ( importProcess == null || ! importProcess.getStatus().isRunning() ) {
			SessionState sessionState = sessionManager.getSessionState();
			File userImportFolder = FileUploadController.getSessionTempDirectory(tempDirectory, sessionState.getSessionId());
			File importFile = new File(userImportFolder, FILE_NAME);
			importProcess = new SamplingDesignImportProcess(samplingDesignManager, surveyId, work, importFile, overwriteAll);
			importProcess.init();
			if ( importFile.exists() && importFile.canRead() ) {
				startProcessThread();
			} else {
				SamplingDesignImportStatus status = importProcess.getStatus();
				status.error();
				status.setErrorMessage(INTERNAL_ERROR_IMPORTING_FILE_MESSAGE_KEY);
			}
		}
		return getStatus();
	}

	protected void startProcessThread() {
		ExecutorServiceUtil.executeInCachedPool(importProcess);
	}
	
	@Secured("ROLE_ADMIN")
	public SamplingDesignImportStatusProxy getStatus() {
		if ( importProcess != null ) {
			SamplingDesignImportStatus status = importProcess.getStatus();
			return new SamplingDesignImportStatusProxy(status);
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
