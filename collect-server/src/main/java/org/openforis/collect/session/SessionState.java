/**
 * 
 */
package org.openforis.collect.session;

import org.openforis.collect.model.User;
import org.openforis.idm.model.Record;

/**
 * @author M. Togna
 * 
 */
public class SessionState {

	/**
	 * @uml.property name="user"
	 */
	private User user;

	/**
	 * Getter of the property <tt>user</tt>
	 * 
	 * @return Returns the user.
	 * @uml.property name="user"
	 */
	public User getUser() {
		return this.user;
	}

	/**
	 * Setter of the property <tt>user</tt>
	 * 
	 * @param user
	 *            The user to set.
	 * @uml.property name="user"
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @uml.property name="sessionId"
	 */
	private String sessionId;

	/**
	 * Getter of the property <tt>sessionId</tt>
	 * 
	 * @return Returns the sessionId.
	 * @uml.property name="sessionId"
	 */
	public String getSessionId() {
		return this.sessionId;
	}

	/**
	 * Setter of the property <tt>sessionId</tt>
	 * 
	 * @param sessionId
	 *            The sessionId to set.
	 * @uml.property name="sessionId"
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * @uml.property name="activeRecord"
	 */
	private Record activeRecord;

	/**
	 * Getter of the property <tt>activeRecord</tt>
	 * 
	 * @return Returns the activeRecord.
	 * @uml.property name="activeRecord"
	 */
	public Record getActiveRecord() {
		return this.activeRecord;
	}

	/**
	 * Setter of the property <tt>activeRecord</tt>
	 * 
	 * @param activeRecord
	 *            The activeRecord to set.
	 * @uml.property name="activeRecord"
	 */
	public void setActiveRecord(Record activeRecord) {
		this.activeRecord = activeRecord;
	}

}