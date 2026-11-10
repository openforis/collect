package org.openforis.collect.web.filter;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Requests that Spring Security must check for a CSRF token: the state-changing ones (not GET, HEAD, TRACE, OPTIONS), except:
 * <ul>
 * <li>/zkau/**: ZK update requests, which ZK already validates against the random desktop id of the user's session</li>
 * <li>/ws/**: SockJS transports, which cannot send custom headers (no @MessageMapping handles client messages)</li>
 * <li>requests with an Authorization header (HTTP Basic clients such as Collect Mobile): browsers cannot add it to a
 * cross-site request, and never cache Basic credentials here because the entry point is the login form</li>
 * </ul>
 */
public class CsrfProtectionMatcher implements RequestMatcher {

	private static final RequestMatcher EXEMPT = new OrRequestMatcher(new AntPathRequestMatcher("/zkau/**"),
			new AntPathRequestMatcher("/ws/**"), new RequestHeaderRequestMatcher("Authorization"));

	@Override
	public boolean matches(HttpServletRequest request) {
		return CsrfFilter.DEFAULT_CSRF_MATCHER.matches(request) && !EXEMPT.matches(request);
	}
}
