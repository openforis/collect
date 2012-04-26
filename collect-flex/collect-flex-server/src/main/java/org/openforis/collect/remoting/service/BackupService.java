package org.openforis.collect.remoting.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.manager.ConfigurationManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.Configuration;
import org.openforis.collect.model.User;
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

	@Autowired
	private ConfigurationManager configurationManager;
	
	@Autowired
	private SessionManager sessionManager;
	
	@Autowired
	private RecordManager recordManager;
	
	@Autowired
	private DataMarshaller dataMarshaller;
	
	private File backupDirectory;
	
	private Map<Integer, Map<String, BackupProcess>> backups;

	public BackupService() {
		this.backups = new HashMap<Integer, Map<String,BackupProcess>>();
	}
	
	public void init() {
		Configuration configuration = configurationManager.getConfiguration();
		String backupPath = configuration.get("backup_path");
	
		backupDirectory = new File(backupPath);
		if ( ! backupDirectory.exists() || ! backupDirectory.canRead() ) {
			throw new IllegalStateException("Cannot access backup directory. Check the configuration.");
		}
	}
	
	public synchronized Map<String, Object> backup(String rootEntityName, List<Integer> ids, Integer stepNumber) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			BackupProcess backup = getBackup(rootEntityName);
			if (backup.isActive()) {
				result.put("error", "Another backup is already active for this survey. Please try later in a few minutes.");
			} else {
				SessionState sessionState = sessionManager.getSessionState();
				User user = sessionState.getUser();
				List<CollectRecord> summaries;
				if ( ids == null ) {
					summaries = getAllRecordSummaries(rootEntityName);
				} else {
					//todo
					summaries = null;
				}
				Step[] steps;
				if ( stepNumber == null || stepNumber <= 0) {
					steps = Step.values();
				} else {
					Step step = Step.valueOf(stepNumber);
					steps = new Step[] {step};
				}
				backup.setRecordSummaries(summaries);
				backup.setUser(user);
				backup.setSteps(steps);
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
		if (backup.isActive()) {
			backup.cancel();
		}
	}
	
	public Map<String, Object> getStatus(String rootEntityName) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		BackupProcess backup = getBackup(rootEntityName);
		result.put("active", backup.isActive());
		result.put("total", backup.getTotal());
		result.put("count", backup.getCount());
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
			backup = new BackupProcess(recordManager, dataMarshaller, backupDirectory, survey, rootEntityName);
			backupsPerSurvey.put(rootEntityName, backup);
		}
		return backup;
	}

	private List<CollectRecord> getAllRecordSummaries(String rootEntityName) {
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey survey = sessionState.getActiveSurvey();
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName, 0, Integer.MAX_VALUE, null, null);
		return summaries;
	}
	
}
