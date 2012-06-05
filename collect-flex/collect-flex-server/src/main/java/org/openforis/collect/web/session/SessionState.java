/**
 * 
 */
package org.openforis.collect.web.session;

import java.util.Locale;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.remoting.service.export.DataExportProcess;

/**
 * @author M. Togna
 * @author G. Miceli
 */
public class SessionState {

	public static String SESSION_ATTRIBUTE_NAME = "sessionState";
	public static final long ACTIVE_RECORD_TIMEOUT = 70000;
	
	private String sessionId;
	private User user;
	private CollectRecord activeRecord;
	private long lastHeartBeatTime;
	private CollectSurvey activeSurvey;
	private Locale locale;
	private DataExportProcess dataExportProcess;

	public boolean isActiveRecordBeingEdited() {
		if (activeRecord != null) {
			long now = System.currentTimeMillis();
			long diff = now - lastHeartBeatTime;
			return diff <= ACTIVE_RECORD_TIMEOUT;
		} else {
			return false;
		}
	}
	
	public void keepActiveRecordAlive() {
		lastHeartBeatTime = System.currentTimeMillis();
	}
	
	public SessionState(String sessionId) {
		this.sessionId = sessionId;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public CollectRecord getActiveRecord() {
		return this.activeRecord;
	}

	public void setActiveRecord(CollectRecord activeRecord) {
		this.activeRecord = activeRecord;
	}

	public CollectSurvey getActiveSurvey() {
		return activeSurvey;
	}

	public void setActiveSurvey(CollectSurvey activeSurvey) {
		this.activeSurvey = activeSurvey;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public DataExportProcess getDataExportProcess() {
		return dataExportProcess;
	}

	public void setDataExportProcess(DataExportProcess dataExportProcess) {
		this.dataExportProcess = dataExportProcess;
	}

}