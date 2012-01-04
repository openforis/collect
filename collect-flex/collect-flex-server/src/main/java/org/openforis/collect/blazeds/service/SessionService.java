/**
 * 
 */
package org.openforis.collect.blazeds.service;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class SessionService {

	// private static Log LOG = LogUtils.getLog(SessionService.class);

	@Autowired
	protected SessionManager sessionManager;

	/**
	 * Method used to keep the session alive
	 */
	//@RemotingInclude
	public void keepAlive() {
		this.sessionManager.keepSessionAlive();
	}

	/**
	 * Return the session state of the active httpsession
	 * 
	 */
	//@RemotingInclude
	public SessionState getSessionState() {
		return this.sessionManager.getSessionState();
	}

}
