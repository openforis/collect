/**
 * 
 */
package org.openforis.collect.web.session;

import java.util.Locale;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;

/**
 * @author M. Togna
 * @author G. Miceli
 */
public class SessionState {

	public static String SESSION_ATTRIBUTE_NAME = "sessionState";

	public enum RecordState {
		NEW, SAVED
	}

	private String sessionId;
	private String activeRecordClientId;
	private User user;
	private CollectRecord activeRecord;
	private CollectSurvey activeSurvey;
	private Locale locale;
	private RecordState activeRecordState;
	private DataExportState dataExtractionState;

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

	public RecordState getActiveRecordState() {
		return activeRecordState;
	}

	public void setActiveRecordState(RecordState activeRecordState) {
		this.activeRecordState = activeRecordState;
	}

	public String getActiveRecordClientId() {
		return activeRecordClientId;
	}

	public void setActiveRecordClientId(String activeRecordClientId) {
		this.activeRecordClientId = activeRecordClientId;
	}

	public DataExportState getDataExtractionState() {
		return dataExtractionState;
	}

	public void setDataExtractionState(
			DataExportState dataExtractionState) {
		this.dataExtractionState = dataExtractionState;
	}
	

}