package org.openforis.collect.web.controller;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author S. Ricci
 * 
 */
@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class SessionController {
	
	@Autowired
	private SessionManager sessionManager;
	
	@RequestMapping(value = "/keepSessionAlive.htm", method = RequestMethod.GET)
	public @ResponseBody String keepSessionAlive(@RequestParam( value="editing", required = false, defaultValue = "false" ) Boolean editing) throws RecordUnlockedException {
		if ( editing ) {
			sessionManager.checkIsActiveRecordLocked();
		}
		return "ok";
	}
	
}
