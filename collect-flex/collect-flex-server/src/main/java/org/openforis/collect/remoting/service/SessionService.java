/**
 * 
 */
package org.openforis.collect.remoting.service;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class SessionService {

	//private static Log LOG = LogFactory.getLog(SessionService.class);

	@Autowired
	protected SessionManager sessionManager;

	/**
	 * Method used to keep the session alive
	 */
//	@Secured("isAuthenticated()")
	public void keepAlive() {
		this.sessionManager.keepSessionAlive();
	}

	/**
	 * Return the session state of the active httpsession
	 * 
	 */
	public SessionState getSessionState() {
		return this.sessionManager.getSessionState();
	}

	/**
	 * Set a locale (language, country) into the session state object
	 * 
	 */
	public void setLocale(String locale) {
		this.sessionManager.setLocale(locale);
	}
	
}
