package org.openforis.collect.remoting.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.remoting.service.dataimport.DataImportExeption;
import org.openforis.collect.remoting.service.dataimport.DataImportProcess;
import org.openforis.collect.remoting.service.dataimport.DataImportState;
import org.openforis.collect.remoting.service.dataimport.DataImportStateProxy;
import org.openforis.collect.remoting.service.dataimport.DataImportSummary;
import org.openforis.collect.remoting.service.dataimport.DataImportSummaryProxy;
import org.openforis.collect.util.ExecutorServiceUtil;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportService {
	
	private static final String IMPORT_PATH = "import";
	
	private static final String FILE_NAME = "data_import.zip";
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SurveyValidator surveyValidator;
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private RecordDao recordDao;
	@Autowired
	private UserManager userManager;
	@Autowired 
	private ServletContext servletContext;
	
	private File packagedFile;
	private File importDirectory;
	private DataImportProcess dataImportProcess;
	
	protected void init() {
		String importRealPath = servletContext.getRealPath(IMPORT_PATH);
		importDirectory = new File(importRealPath);
		if ( importDirectory.exists() ) {
			importDirectory.delete();
		}
		if ( ! importDirectory.mkdirs() && ! importDirectory.canRead() ) {
			throw new IllegalStateException("Cannot access import directory: " + importRealPath);
		}
	}
	
	@Secured("ROLE_ADMIN")
	public DataImportStateProxy startSummaryCreation(String selectedSurveyUri, boolean overwriteAll) throws DataImportExeption {
		if ( dataImportProcess == null || ! dataImportProcess.isRunning() ) {
			SessionState sessionState = sessionManager.getSessionState();
			File userImportFolder = new File(importDirectory, sessionState.getSessionId());
			packagedFile = new File(userImportFolder, FILE_NAME);
			List<User> usersList = userManager.loadAll();
			HashMap<String, User> users = new HashMap<String, User>();
			for (User user : usersList) {
				users.put(user.getName(), user);
			}
			dataImportProcess = new DataImportProcess(surveyManager, surveyValidator, recordManager, recordDao, selectedSurveyUri, users, packagedFile, overwriteAll);
			dataImportProcess.prepareToStartSummaryCreation();
			ExecutorServiceUtil.executeInCachedPool(dataImportProcess);
		}
		DataImportState state = dataImportProcess.getState();
		DataImportStateProxy proxy = new DataImportStateProxy(state);
		return proxy;
	}
	
	@Secured("ROLE_ADMIN")
	public DataImportStateProxy startImport(List<Integer> entryIdsToImport, String surveyName) throws Exception {
		dataImportProcess.setEntryIdsToImport(entryIdsToImport);
		dataImportProcess.setNewSurveyName(surveyName);
		dataImportProcess.prepareToStartImport();
		ExecutorServiceUtil.executeInCachedPool(dataImportProcess);
		DataImportState state = dataImportProcess.getState();
		DataImportStateProxy proxy = new DataImportStateProxy(state);
		return proxy;
	}
	
	@Secured("ROLE_ADMIN")
	public DataImportStateProxy getState() {
		if ( dataImportProcess != null ) {
			DataImportState state = dataImportProcess.getState();
			DataImportStateProxy proxy = new DataImportStateProxy(state);
			return proxy;
		} else {
			return null;
		}
	}
	
	@Secured("ROLE_ADMIN")
	public DataImportSummaryProxy getSummary() {
		if ( dataImportProcess != null ) {
			DataImportSummary summary = dataImportProcess.getSummary();
			DataImportSummaryProxy proxy = new DataImportSummaryProxy(summary);
			return proxy;
		} else {
			return null;
		}
	}

	@Secured("ROLE_ADMIN")
	public void cancel() {
		if ( dataImportProcess != null ) {
			dataImportProcess.cancel();
		}
	}
	
}
