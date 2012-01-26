/**
 * 
 */
package org.openforis.collect.session;

import java.util.Locale;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.User;
import org.openforis.idm.metamodel.Survey;

/**
 * @author M. Togna
 * @author G. Miceli
 */
public class SessionState {

	public static String SESSION_ATTRIBUTE_NAME = "sessionState";
	
	public enum RecordState {
		NEW, SAVED
	}
	
	private User user;
	private String sessionId;
	private CollectRecord activeRecord;
	private Survey activeSurvey;
	private Locale locale;
	private RecordState activeRecordState;

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

	public Survey getActiveSurvey() {
		return activeSurvey;
	}

	public void setActiveSurvey(Survey activeSurvey) {
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
}