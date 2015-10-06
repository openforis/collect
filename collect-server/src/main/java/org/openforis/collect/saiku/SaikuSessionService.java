package org.openforis.collect.saiku;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.SessionManager;
import org.saiku.web.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 
 * @author S. Ricci
 *
 */
public class SaikuSessionService extends SessionService {

	private static final Log LOG = LogFactory.getLog(SaikuSessionService.class);

	@Autowired
	@Qualifier("sessionManager")
	private SessionManager sessionManager;
	
	private Map<String, Map<String, Object>> sessionInfoByUserName = new HashMap<String, Map<String, Object>>();

	@Override
	public Map<String, Object> getSession() throws Exception {
		if (isAuthenticated()) {
			Authentication auth = SecurityContextHolder.getContext()
					.getAuthentication();
			Map<String, Object> r = new HashMap<String, Object>();
			Object p = auth.getPrincipal();
			String authUser = p instanceof UserDetails ? ((UserDetails) p)
					.getUsername() : p.toString();
			if (! sessionInfoByUserName.containsKey(authUser)) {
				LOG.info(String.format("Creating session info for user %s", authUser));
				sessionInfoByUserName.put(authUser, initSessionInfo(authUser));
			}
			r.putAll(sessionInfoByUserName.get(authUser));
			return r;
		} else {
			return new HashMap<String, Object>();
		}
	}

	@Override
	public Map<String, Object> login(HttpServletRequest req, String username,
			String password) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void logout(HttpServletRequest req) {
		throw new UnsupportedOperationException();
	}

	public void clearSession(String userName) {
		if (sessionInfoByUserName.containsKey(userName)) {
			sessionInfoByUserName.remove(userName);
		}
	}
	
	private Map<String, Object> initSessionInfo(String username) {
		Map<String, Object> session = new HashMap<String, Object>();

		session.put("username", username);
		session.put("sessionid", UUID.randomUUID().toString());
		session.put("authid", RequestContextHolder
				.currentRequestAttributes().getSessionId());
		List<String> roles = new ArrayList<String>();
		for (GrantedAuthority ga : SecurityContextHolder.getContext()
				.getAuthentication().getAuthorities()) {
			roles.add(ga.getAuthority());
		}
		session.put("roles", roles);
		return session;
	}

	private boolean isAuthenticated() {
		return SecurityContextHolder.getContext() != null
				&& SecurityContextHolder.getContext().getAuthentication() != null;
	}

	public void setAllowAnonymous(Boolean allow) {
		//UNUSED
	}

}
