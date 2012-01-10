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

	private User user;
	private String sessionId;
	private CollectRecord activeRecord;
	private Survey activeSurvey;
	private Locale locale;
	
	/**
	 * Getter of the property <tt>user</tt>
	 * 
	 * @return Returns the user.
	 */
	public User getUser() {
		return this.user;
	}

	/**
	 * Setter of the property <tt>user</tt>
	 * 
	 * @param user
	 *            The user to set.
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Getter of the property <tt>sessionId</tt>
	 * 
	 * @return Returns the sessionId.
	 */
	public String getSessionId() {
		return this.sessionId;
	}

	/**
	 * Setter of the property <tt>sessionId</tt>
	 * 
	 * @param sessionId
	 *            The sessionId to set.
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Getter of the property <tt>activeRecord</tt>
	 * 
	 * @return Returns the activeRecord.
	 */
	public CollectRecord getActiveRecord() {
		return this.activeRecord;
	}

	/**
	 * Setter of the property <tt>activeRecord</tt>
	 * 
	 * @param activeRecord
	 *            The activeRecord to set.
	 */
	public void setActiveRecord(CollectRecord activeRecord) {
		this.activeRecord = activeRecord;
	}

	public Survey getActiveSurvey() {
		return activeSurvey;
	}

	public void setActiveSurvey(Survey activeSurvey) {
		this.activeSurvey = activeSurvey;
	}

	protected Locale getLocale() {
		return locale;
	}

	protected void setLocale(Locale locale) {
		this.locale = locale;
	}
}