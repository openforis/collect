/**
 * 
 */
package org.openforis.collect.spring.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.LastModified;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;

/**
 * @author M. Togna
 * 
 */
public class UrlFilenameHandlerAdapter implements HandlerAdapter {

	/**
	 * 
	 */
	public UrlFilenameHandlerAdapter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.HandlerAdapter#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Object handler) {
		return handler.getClass().isAssignableFrom(UrlFilenameViewController.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.HandlerAdapter#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
	 * java.lang.Object)
	 */
	@Override
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String viewName = this.getViewName(request);
		return new ModelAndView(viewName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.HandlerAdapter#getLastModified(javax.servlet.http.HttpServletRequest, java.lang.Object)
	 */
	@Override
	public long getLastModified(HttpServletRequest request, Object handler) {
		if (handler instanceof LastModified) {
			return ((LastModified) handler).getLastModified(request);
		}
		return -1L;
	}

	String getViewName(HttpServletRequest request) {
		String root = request.getContextPath();
		String uri = request.getRequestURI();
		int begin = root.length();

		int end;
		if (uri.indexOf(";") != -1) {
			end = uri.indexOf(";");
		} else if (uri.indexOf("?") != -1) {
			end = uri.indexOf("?");
		} else {
			end = uri.length();
		}

		String fileName = uri.substring(begin, end);
		if (fileName.indexOf(".") != -1) {
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
		}

		return fileName;
	}

}
