package org.openforis.collect.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openforis.collect.web.session.SessionState;
import org.openforis.commons.web.Response;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
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
	
	protected Response generateFormValidationResponse(BindingResult result) {
		List<ObjectError> errors = result.getAllErrors();
		Response response = new Response();
		if (! errors.isEmpty()) {
			response.setErrorStatus();
			response.addObject("errors", errors);
		}
		return response;
	}
	
}
