/**
 * 
 */
package org.openforis.collect.blazeds.service;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.remoting.RemotingInclude;

/**
 * @author Mino Togna
 * 
 */
public class SessionService {

//	private static Log LOG = LogUtils.getLog(SessionService.class);

	@Autowired
	protected SessionManager sessionManager;

	/**
	 * Method used to keep the session alive
	 */
	@RemotingInclude
	public void keepAlive() {
		sessionManager.keepSessionAlive();
	}

	/**
	 * Return the session state of the active httpsession
	 * 
	 */
	@RemotingInclude
	public SessionState getSessionState() {
		return sessionManager.getSessionState();
	}

}
