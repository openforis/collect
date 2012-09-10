/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.manager.DatabaseVersionManager;
import org.openforis.collect.manager.DatabaseVersionNotCompatibleException;
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

	@Autowired
	protected DatabaseVersionManager databaseVersionManager;

	/**
	 * Method used to keep the session alive
	 * @throws RecordUnlockedException 
	 */
	//@Secured("isAuthenticated()")
	@Transactional
	public void keepAlive(Boolean editing) throws RecordUnlockedException {
		sessionManager.keepSessionAlive();
		if(editing) {
			sessionManager.checkIsActiveRecordLocked();
		}
	}
	
	/**
	 * Set a locale (language, country) into the session state object
	 * 
	 * @return map with user, sessionId
	 * @throws DatabaseVersionNotCompatibleException 
	 */
	//@Secured("isAuthenticated()")
	@Transactional
	public Map<String, Object> initSession(String locale) throws DatabaseVersionNotCompatibleException {
		databaseVersionManager.checkIsVersionCompatible();
		
		sessionManager.setLocale(locale);
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState.getUser();
		UserProxy userProxy = new UserProxy(user);
		String sessionId = sessionState.getSessionId();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("user", userProxy);
		result.put("sessionId", sessionId);
		return result;
	}
	
	//@Secured("isAuthenticated()")
	public void logout() {
		sessionManager.invalidateSession();
	}
	
}
