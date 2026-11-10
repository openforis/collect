package org.openforis.collect.web.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.openforis.collect.config.CollectConfiguration;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class CollectCorsFilterTest {

	@After
	public void resetDevelopmentMode() {
		CollectConfiguration.setDevelopmentMode(false);
	}

	@Test
	public void testDevelopmentModeAllowsFrontendWithCsrfHeader() throws Exception {
		CollectConfiguration.setDevelopmentMode(true);
		MockHttpServletResponse response = filter();
		assertEquals(CollectConfiguration.DEV_FRONTEND_ORIGIN, response.getHeader("Access-Control-Allow-Origin"));
		assertTrue(response.getHeader("Access-Control-Allow-Headers").contains("X-XSRF-TOKEN"));
	}

	@Test
	public void testNoCorsHeadersInProduction() throws Exception {
		assertNull(filter().getHeader("Access-Control-Allow-Origin"));
	}

	private MockHttpServletResponse filter() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		new CollectCorsFilter().doFilter(new MockHttpServletRequest("POST", "/api/user"), response, new MockFilterChain());
		return response;
	}
}
