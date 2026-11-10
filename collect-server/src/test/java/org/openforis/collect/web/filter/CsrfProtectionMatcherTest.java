package org.openforis.collect.web.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class CsrfProtectionMatcherTest {

	private final CsrfProtectionMatcher matcher = new CsrfProtectionMatcher();

	@Test
	public void testStateChangingBrowserRequestsAreProtected() {
		assertTrue(matcher.matches(request("POST", "/api/user", null)));
		assertTrue(matcher.matches(request("DELETE", "/api/survey/1", null)));
		assertTrue(matcher.matches(request("POST", "/perform_login", null)));
		assertTrue(matcher.matches(request("POST", "/preview_save-data-expanded", null)));
		assertTrue(matcher.matches(request("POST", "/zkauX", null)));
	}

	@Test
	public void testExemptRequests() {
		assertFalse(matcher.matches(request("GET", "/api/user", null)));
		assertFalse(matcher.matches(request("POST", "/zkau", null)));
		assertFalse(matcher.matches(request("POST", "/zkau/upload", null)));
		assertFalse(matcher.matches(request("POST", "/ws/123/abc/xhr_send", null)));
		assertFalse(matcher.matches(request("POST", "/api/surveys/restore/data", "Basic YWRtaW46cGFzcw==")));
	}

	private static MockHttpServletRequest request(String method, String servletPath, String authorization) {
		MockHttpServletRequest request = new MockHttpServletRequest(method, "/collect" + servletPath);
		request.setContextPath("/collect");
		request.setServletPath(servletPath);
		if (authorization != null) {
			request.addHeader("Authorization", authorization);
		}
		return request;
	}
}
