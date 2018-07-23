package org.openforis.collect.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author S. Ricci
 * 
 * Controller that manages the unlocking of active record
 * when closing browser's window
 *
 */
@Controller
public class UnlockController {
	private static final Logger LOG = LogManager.getLogger(UnlockController.class);
	
	@Autowired
	private RecordManager recordManager;
	
	@RequestMapping(value = "/clearActiveRecord.htm", method = RequestMethod.POST)
	public @ResponseBody String clearActiveRecord(HttpServletRequest request) {
		try {
			HttpSession session = request.getSession();
			if(session != null) {
				SessionState sessionState = (SessionState) session.getAttribute(SessionState.SESSION_ATTRIBUTE_NAME);
				if(sessionState != null) {
					CollectRecord activeRecord = sessionState.getActiveRecord();
					if(activeRecord != null) {
						Integer recordId = activeRecord.getId();
						if ( recordId != null) {
							recordManager.releaseLock(recordId);
						}
						//clear session state
						sessionState.setActiveRecord(null);
					}
				}
			}
			return "ok";
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}
	
}
