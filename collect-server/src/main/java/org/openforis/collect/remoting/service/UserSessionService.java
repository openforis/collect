/**
 * 
 */
package org.openforis.collect.remoting.service;

import org.openforis.collect.manager.OperationResult;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.manager.UserPersistenceException;
import org.openforis.collect.model.User;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 */
public class UserSessionService {

	@Autowired
	private UserManager userManager;
	@Autowired
	private RecordSessionManager sessionManager;

	public OperationResult changePassword(String oldPassword, String newPassword) throws UserPersistenceException {
		SessionState sessionState = sessionManager.getSessionState();
		User currentUser = sessionState.getUser();
		return userManager.changePassword(currentUser.getUsername(), oldPassword, newPassword);
	}
	
}
