/**
 * 
 */
package org.openforis.collect.blazeds.service;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.openforis.collect.model.SessionState;
import org.openforis.collect.util.LogUtils;
import org.springframework.flex.remoting.RemotingInclude;

import flex.messaging.FlexContext;

/**
 * @author Mino Togna
 * 
 */
public class SessionService {

	private static final String KEEP_ALIVE_SESSION_PARAMETER = "keepAlive";

	private static Log LOG = LogUtils.getLog(SessionService.class);

	/**
	 * Method used to keep the session alive
	 */
	@RemotingInclude
	public void keepAlive() {
		HttpSession session = FlexContext.getHttpRequest().getSession();
		session.setAttribute(KEEP_ALIVE_SESSION_PARAMETER, new Date());
		if (LOG.isDebugEnabled()) {
			LOG.debug("Keep alive request received");
		}
	}

	/**
	 * Return the session state of the active httpsession
	 * 
	 */
	@RemotingInclude
	public SessionState getSessionState() {
		SessionState sessionState = (SessionState) FlexContext.getHttpRequest().getSession().getAttribute(SessionState.SESSION_STATE_SESSION_ATTRIBUTE_NAME);
		if (sessionState == null) {
			sessionState = new SessionState();
			FlexContext.getHttpRequest().getSession().setAttribute(SessionState.SESSION_STATE_SESSION_ATTRIBUTE_NAME, sessionState);
		}
		return sessionState;
	}

}
