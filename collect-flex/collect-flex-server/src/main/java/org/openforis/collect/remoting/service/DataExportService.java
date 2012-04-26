package org.openforis.collect.remoting.service;

import java.io.File;

import org.openforis.collect.manager.ConfigurationManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.Configuration;
import org.openforis.collect.model.User;
import org.openforis.collect.remoting.service.export.DataExportProcess;
import org.openforis.collect.remoting.service.export.DataExportState;
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

	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private RecordManager recordManager;
	
	@Autowired
	private ConfigurationManager configurationManager;
	
	private File exportDirectory;
	
	public void init() {
		Configuration configuration = configurationManager.getConfiguration();
		String tempPath = configuration.get("export_path");
		exportDirectory = new File(tempPath);
	}

	@Transactional
	/**
	 * 
	 * @param rootEntityName
	 * @param entityId
	 * @param stepNumber
	 * @return false if another export has been launched by the current user, true otherwise
	 */
	public boolean export(String rootEntityName, int entityId, int stepNumber) {
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState.getUser();
		DataExportState state = sessionState.getDataExportState();
		if ( state == null || ! state.isRunning() ) {
			File exportDir = new File(exportDirectory, user.getName());
			if ( ! exportDir.exists() && ! exportDir.mkdirs() ) {
				throw new IllegalStateException("Cannot create temp directory");
			}
			state = new DataExportState();
			sessionState.setDataExportState(state);
			CollectSurvey survey = sessionState.getActiveSurvey();
			DataExportProcess process = new DataExportProcess(recordManager, exportDir, state, survey, rootEntityName, entityId, Step.valueOf(stepNumber));
			ExecutorServiceUtil.executeInCachedPool(process);
			return true;
		} else {
			return false;
		}
	}
	
	public void cancel() {
		SessionState sessionState = sessionManager.getSessionState();
		DataExportState state = sessionState.getDataExportState();
		if ( state != null ) {
			state.setCancelled(true);
		}
	}

	public DataExportState getState() {
		SessionState sessionState = sessionManager.getSessionState();
		DataExportState state = sessionState.getDataExportState();
		return state;
	}
	
}
