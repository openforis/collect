package org.openforis.collect.remoting.service;

import java.io.File;

import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.speciesimport.SpeciesImportProcess;
import org.openforis.collect.manager.speciesimport.SpeciesImportStatus;
import org.openforis.collect.remoting.service.dataimport.DataImportExeption;
import org.openforis.collect.remoting.service.speciesimport.proxy.SpeciesImportStatusProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public class SpeciesImportService extends ReferenceDataImportService<SpeciesImportStatusProxy, SpeciesImportProcess> {
	
	private static final String IMPORT_FILE_NAME = "species.csv";
	
	@Autowired
	private SpeciesManager speciesManager;
	
	public SpeciesImportService() {
		super(IMPORT_FILE_NAME);
	}
	
	@Secured("ROLE_ADMIN")
	public SpeciesImportStatusProxy start(int taxonomyId, boolean overwriteAll) throws DataImportExeption {
		if ( importProcess == null || ! importProcess.getStatus().isRunning() ) {
			File importFile = getImportFile();
			importProcess = new SpeciesImportProcess(speciesManager, taxonomyId, importFile, overwriteAll);
			importProcess.init();
			SpeciesImportStatus status = importProcess.getStatus();
			if ( status != null && ! importProcess.getStatus().isError() ) {
				startProcessThread();
			}
		}
		return getStatus();
	}

	@Secured("ROLE_ADMIN")
	public SpeciesImportStatusProxy getStatus() {
		if ( importProcess != null ) {
			SpeciesImportStatus status = importProcess.getStatus();
			return new SpeciesImportStatusProxy(status);
		} else {
			return null;
		}
	}
	
}
