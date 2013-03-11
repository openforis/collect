package org.openforis.collect.remoting.service;

import java.io.File;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.codeListImport.CodeListImportProcess;
import org.openforis.collect.manager.codeListImport.CodeListImportStatus;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.remoting.service.codeListImport.proxy.CodeListImportStatusProxy;
import org.openforis.collect.remoting.service.dataImport.DataImportExeption;
import org.openforis.idm.metamodel.CodeList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListImportService extends ReferenceDataImportService<CodeListImportStatusProxy, CodeListImportProcess> {
	
	private static final String IMPORT_FILE_NAME = "code_list.csv";
	
	@Autowired
	private SessionManager sessionManager;
	
	public CodeListImportService() {
		super(IMPORT_FILE_NAME);
	}
	
	@Secured("ROLE_ADMIN")
	public CodeListImportStatusProxy start(int codeListId, boolean overwriteData) throws DataImportExeption {
		if ( importProcess == null || ! importProcess.getStatus().isRunning() ) {
			File importFile = getImportFile();
			SessionStatus designerSessionStatus = sessionManager.getDesignerSessionStatus();
			CollectSurvey survey = designerSessionStatus.getSurvey();
			String langCode = designerSessionStatus.getCurrentLanguageCode();
			CodeList codeList = survey.getCodeListById(codeListId);
			importProcess = new CodeListImportProcess(codeList, langCode, importFile, overwriteData);
			importProcess.init();
			CodeListImportStatus status = importProcess.getStatus();
			if ( status != null && ! importProcess.getStatus().isError() ) {
				startProcessThread();
			}
		}
		return getStatus();
	}

	@Secured("ROLE_ADMIN")
	public CodeListImportStatusProxy getStatus() {
		if ( importProcess != null ) {
			CodeListImportStatus status = importProcess.getStatus();
			return new CodeListImportStatusProxy(status);
		} else {
			return null;
		}
	}
	
	
}
