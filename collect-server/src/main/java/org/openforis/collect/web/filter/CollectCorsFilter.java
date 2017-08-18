package org.openforis.collect.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CollectCorsFilter extends OncePerRequestFilter {

	private String getAllowedOrigins() {
		return "http://localhost:3000";
	}

	@Override
	public void destroy() {
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", getAllowedOrigins());
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Headers", "credentials, Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
		filterChain.doFilter(request, response);
	}
}