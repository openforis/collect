package org.openforis.collect.remoting.service;

import java.io.File;

import javax.servlet.ServletContext;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.dataexport.BackupProcess;
import org.openforis.collect.manager.dataexport.DataExportStatus;
import org.openforis.collect.manager.dataexport.CSVDataExportProcess;
import org.openforis.collect.manager.dataexport.proxy.DataExportStatusProxy;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.openforis.collect.utils.ExecutorServiceUtil;
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
	private SurveyManager surveyManager;
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private RecordFileManager recordFileManager;
	@Autowired
	private CodeListManager codeListManager;
	@Autowired
	private DataMarshaller dataMarshaller;
	@Autowired 
	private ServletContext servletContext;
	
	private File exportDirectory;
	
	private AbstractProcess<Void, DataExportStatus> dataExportProcess;

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
	public DataExportStatusProxy export(String rootEntityName, int stepNumber, Integer entityId, boolean includeAllAncestorAttributes) {
		if ( dataExportProcess == null || ! dataExportProcess.getStatus().isRunning() ) {
			SessionState sessionState = sessionManager.getSessionState();
			File exportDir = new File(exportDirectory, sessionState.getSessionId());
			if ( ! exportDir.exists() && ! exportDir.mkdirs() ) {
				throw new IllegalStateException("Cannot create export directory: " + exportDir.getAbsolutePath());
			}
			CollectSurvey survey = sessionState.getActiveSurvey();
			File outputFile = new File(exportDir, "data.zip");
			CSVDataExportProcess process = new CSVDataExportProcess(outputFile,
					recordManager, survey, rootEntityName, Step.valueOf(stepNumber), entityId, includeAllAncestorAttributes);
			process.init();
			dataExportProcess = process;
			ExecutorServiceUtil.executeInCachedPool(process);
		}
		return getState();
	}
	
	@Transactional
	public DataExportStatusProxy fullExport(String rootEntityName, int[] stepNumbers) {
		if ( dataExportProcess == null || ! dataExportProcess.getStatus().isRunning() ) {
			SessionState sessionState = sessionManager.getSessionState();
			File exportDir = new File(exportDirectory, sessionState.getSessionId());
			if ( ! exportDir.exists() && ! exportDir.mkdirs() ) {
				throw new IllegalStateException("Cannot create export directory: " + exportDir.getAbsolutePath());
			}
			CollectSurvey survey = sessionState.getActiveSurvey();
			Step[] steps = toStepsArray(stepNumbers);
			BackupProcess process = new BackupProcess(surveyManager, recordManager, recordFileManager,
					dataMarshaller, exportDir, survey, rootEntityName, steps);
			process.init();
			dataExportProcess = process;
			ExecutorServiceUtil.executeInCachedPool(process);
		}
		return getState();
	}

	private Step[] toStepsArray(int[] stepNumbers) {
		if ( stepNumbers == null ) {
			return Step.values();
		} else {
			Step[] steps = new Step[stepNumbers.length];
			for (int i = 0; i < stepNumbers.length; i++ ) {
				int stepNum = stepNumbers[i];
				Step step = Step.valueOf(stepNum);
				steps[i] = step;
			}
			return steps;
		}
	}

	public void cancel() {
		if ( dataExportProcess != null ) {
			dataExportProcess.cancel();
		}
	}

	public DataExportStatusProxy getState() {
		if ( dataExportProcess != null ) {
			return new DataExportStatusProxy(dataExportProcess.getStatus());
		} else {
			return null;
		}
	}
	
}
