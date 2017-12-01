package org.openforis.collect.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openforis.collect.web.session.SessionState;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * @author S. Ricci
 * 
 */
@CrossOrigin(maxAge = 3600)
public abstract class BasicController {
	
	protected SessionState getSessionState(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if(session != null) {
			SessionState sessionState = (SessionState) session.getAttribute(SessionState.SESSION_ATTRIBUTE_NAME);
			return sessionState;
		}
		return null;
	}
	
}
