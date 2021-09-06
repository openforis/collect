package org.openforis.collect.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.config.CollectConfiguration;
import org.springframework.web.filter.OncePerRequestFilter;

public class CollectCorsFilter extends OncePerRequestFilter {

	private static final String ALLOWED_DEV_ORIGINS = "http://127.0.0.1:3000/";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (CollectConfiguration.isDevelopmentMode()) {
			response.addHeader("Access-Control-Allow-Origin", ALLOWED_DEV_ORIGINS);
			response.setHeader("Access-Control-Allow-Methods", "DELETE, GET, OPTIONS, PATCH, POST, PUT");
			response.setHeader("Access-Control-Allow-Credentials", "true");
			response.setHeader("Access-Control-Allow-Headers", "credentials, Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
		}
		filterChain.doFilter(request, response);
	}
}