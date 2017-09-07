package org.openforis.collect.utils;

import javax.servlet.http.HttpSession;

public class HttpSessions {
	
	public static boolean isDevelopmentMode(HttpSession session) {
		return Boolean.valueOf(session.getServletContext().getInitParameter("developmentMode"));
	}
}
