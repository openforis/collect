/**
 * 
 */
package org.openforis.collect.remoting.service;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.UserProxy;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class SessionService {

	//private static Log LOG = LogFactory.getLog(SessionService.class);

	@Autowired
	protected SessionManager sessionManager;

	/**
	 * Method used to keep the session alive
	 * @throws RecordUnlockedException 
	 */
//	@Secured("isAuthenticated()")
	@Transactional
	public void keepAlive(Boolean editing) throws RecordUnlockedException {
		this.sessionManager.keepSessionAlive();
		if(editing) {
			sessionManager.checkIsLocking();
		}
	}
	
	/**
	 * Set a locale (language, country) into the session state object
	 * 
	 * @return logged user
	 */
	public UserProxy initSession(String locale) {
		this.sessionManager.setLocale(locale);
		
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState.getUser();
		UserProxy userProxy = new UserProxy(user);
		return userProxy;
	}
	
}
