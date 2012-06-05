package org.openforis.collect.remoting.service;

import java.io.File;

import javax.servlet.ServletContext;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.openforis.collect.remoting.service.backup.BackupProcess;
import org.openforis.collect.remoting.service.export.DataExportProcess;
import org.openforis.collect.remoting.service.export.DataExportState;
import org.openforis.collect.remoting.service.export.SelectiveDataExportProcess;
import org.openforis.collect.util.ExecutorServiceUtil;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportService {

	//private static Log LOG = LogFactory.getLog(DataExportService.class);

	private static final String EXPORT_PATH = "export";
	
	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private RecordManager recordManager;
	
	@Autowired
	private DataMarshaller dataMarshaller;
	
	@Autowired 
	private ServletContext servletContext;
	
	private File exportDirectory;
	
	public void init() {
		String exportRealPath = servletContext.getRealPath(EXPORT_PATH);
		exportDirectory = new File(exportRealPath);
		if ( exportDirectory.exists() ) {
			exportDirectory.delete();
		}
		if ( ! exportDirectory.mkdirs() && ! exportDirectory.canRead() ) {
			throw new IllegalStateException("Cannot access export directory: " + exportRealPath);
		}
	}

	/**
	 * 
	 * @param rootEntityName
	 * @param entityId
	 * @param stepNumber
	 * @return state of the export
	 */
	@Transactional
	public DataExportState export(String rootEntityName, int stepNumber, int entityId) {
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState.getUser();
		DataExportProcess dataExportProcess = sessionState.getDataExportProcess();
		if ( dataExportProcess == null || ! dataExportProcess.isRunning() ) {
			File exportDir = new File(exportDirectory, user.getName());
			if ( ! exportDir.exists() && ! exportDir.mkdirs() ) {
				throw new IllegalStateException("Cannot create export directory: " + exportDir.getAbsolutePath());
			}
			CollectSurvey survey = sessionState.getActiveSurvey();
			SelectiveDataExportProcess process = new SelectiveDataExportProcess(recordManager, exportDir, survey, rootEntityName, entityId, Step.valueOf(stepNumber));
			dataExportProcess = process;
			sessionState.setDataExportProcess(process);
			ExecutorServiceUtil.executeInCachedPool(process);
		}
		return dataExportProcess.getState();
	}
	
	@Transactional
	public DataExportState fullExport(String rootEntityName, int[] stepNumbers) {
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState.getUser();
		DataExportProcess dataExportProcess = sessionState.getDataExportProcess();
		if ( dataExportProcess == null || ! dataExportProcess.isRunning() ) {
			File exportDir = new File(exportDirectory, user.getName());
			if ( ! exportDir.exists() && ! exportDir.mkdirs() ) {
				throw new IllegalStateException("Cannot create export directory: " + exportDir.getAbsolutePath());
			}
			CollectSurvey survey = sessionState.getActiveSurvey();
			if ( stepNumbers == null ) {
				stepNumbers = getAllStepNumbers();
			}
			BackupProcess process = new BackupProcess(recordManager, dataMarshaller, exportDir, survey, rootEntityName, stepNumbers);
			dataExportProcess = process;
			sessionState.setDataExportProcess(process);
			ExecutorServiceUtil.executeInCachedPool(process);
		}
		return dataExportProcess.getState();
	}

	private int[] getAllStepNumbers() {
		int[] stepNumbers;
		Step[] steps = Step.values();
		stepNumbers = new int[steps.length];
		int i = 0;
		for (Step step : steps) {
			stepNumbers[i++] = step.getStepNumber();
		}
		return stepNumbers;
	}
	
	public void cancel() {
		SessionState sessionState = sessionManager.getSessionState();
		DataExportProcess dataExportProcess = sessionState.getDataExportProcess();
		if ( dataExportProcess != null ) {
			dataExportProcess.cancel();
		}
	}

	public DataExportState getState() {
		SessionState sessionState = sessionManager.getSessionState();
		DataExportProcess dataExportProcess = sessionState.getDataExportProcess();
		if ( dataExportProcess != null ) {
			return dataExportProcess.getState();
		}
		return null;
	}
	
}
