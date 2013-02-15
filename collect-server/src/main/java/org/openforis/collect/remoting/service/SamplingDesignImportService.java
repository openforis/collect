package org.openforis.collect.remoting.service;

import java.io.File;

import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.samplingDesignImport.SamplingDesignImportProcess;
import org.openforis.collect.manager.samplingDesignImport.SamplingDesignImportStatus;
import org.openforis.collect.remoting.service.dataImport.DataImportExeption;
import org.openforis.collect.remoting.service.samplingDesignImport.proxy.SamplingDesignImportStatusProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignImportService extends ReferenceDataImportService<SamplingDesignImportStatusProxy, SamplingDesignImportProcess> {
	
	private static final String INTERNAL_ERROR_IMPORTING_FILE_MESSAGE_KEY = "samplingDesignImport.error.internalErrorImportingFile";

	private static final String IMPORT_FILE_NAME = "sampling_design.csv";
	
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	
	public SamplingDesignImportService() {
		super(IMPORT_FILE_NAME);
	}
	
	@Secured("ROLE_ADMIN")
	public SamplingDesignImportStatusProxy start(int surveyId, boolean work, boolean overwriteAll) throws DataImportExeption {
		if ( importProcess == null || ! importProcess.getStatus().isRunning() ) {
			File importFile = getImportFile();
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

	@Secured("ROLE_ADMIN")
	public SamplingDesignImportStatusProxy getStatus() {
		if ( importProcess != null ) {
			SamplingDesignImportStatus status = importProcess.getStatus();
			return new SamplingDesignImportStatusProxy(status);
		} else {
			return null;
		}
	}
	
	
}
