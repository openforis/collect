package org.openforis.collect.remoting.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.openforis.collect.remoting.service.backup.BackupProcess;
import org.openforis.collect.util.ExecutorServiceUtil;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class BackupService {

	private static final String BACKUP_PATH = "backup";
	
	@Autowired
	private SessionManager sessionManager;
	
	@Autowired
	private SurveyManager surveyManager;
	
	@Autowired
	private RecordManager recordManager;
	
	@Autowired
	private DataMarshaller dataMarshaller;
	
	@Autowired 
	private ServletContext servletContext;
	
	private File backupDirectory;
	
	private Map<Integer, Map<String, BackupProcess>> backups;

	public BackupService() {
		this.backups = new HashMap<Integer, Map<String,BackupProcess>>();
	}
	
	public void init() {
		String backupRealPath = servletContext.getRealPath(BACKUP_PATH);
		backupDirectory = new File(backupRealPath);
		if ( ! backupDirectory.exists() ) {
			backupDirectory.mkdirs();
			if (! backupDirectory.canRead() ) {
				throw new IllegalStateException("Cannot access backup directory. Check the configuration.");
			}
		}
	}
	
	public synchronized Map<String, Object> backup(String rootEntityName, List<Integer> ids, Integer stepNumber) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			BackupProcess backup = getBackup(rootEntityName);
			if (backup.isRunning()) {
				result.put("error", "Another backup is already active for this survey. Please try later in a few minutes.");
			} else {
				ExecutorServiceUtil.executeInCachedPool(backup);
				result.put("start", "The backup is started");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void cancel(String rootEntityName) throws Exception {
		BackupProcess backup = getBackup(rootEntityName);
		if (backup.isRunning()) {
			backup.cancel();
		}
	}
	
	public Map<String, Object> getStatus(String rootEntityName) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		BackupProcess backup = getBackup(rootEntityName);
		result.put("active", backup.isRunning());
		//result.put("total", backup.getTotal());
		//result.put("count", backup.getCount());
		return result;
	}
	
	private BackupProcess getBackup(String rootEntityName) throws Exception {
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey survey = sessionState.getActiveSurvey();
		Integer surveyId = survey.getId();
		Map<String, BackupProcess> backupsPerSurvey = backups.get(surveyId);
		if ( backupsPerSurvey == null ) {
			backupsPerSurvey = new HashMap<String, BackupProcess>();
			backups.put(surveyId, backupsPerSurvey);
		}
		BackupProcess backup = backupsPerSurvey.get(rootEntityName);
		if (backup == null) {
			int[] stepNumbers = {1, 2, 3};
			backup = new BackupProcess(surveyManager, recordManager, dataMarshaller, backupDirectory, survey, rootEntityName, stepNumbers );
			backupsPerSurvey.put(rootEntityName, backup);
		}
		return backup;
	}

}
