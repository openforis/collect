package org.openforis.collect.web.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openforis.collect.web.session.SessionState;
import org.springframework.stereotype.Controller;

/**
 * @author S. Ricci
 * 
 */
@Controller
public class BasicController {

	protected SessionState getSessionState(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if(session != null) {
			SessionState sessionState = (SessionState) session.getAttribute(SessionState.SESSION_ATTRIBUTE_NAME);
			return sessionState;
		}
		return null;
	}
	
	protected void writeFileToResponse(HttpServletResponse response, File file) throws IOException {
		FileInputStream is = null;
		BufferedInputStream buf = null;
		try {
			String name = file.getName();
			FileTypeMap defaultFileTypeMap = MimetypesFileTypeMap.getDefaultFileTypeMap();
			String contentType = defaultFileTypeMap.getContentType(name);
			response.setContentType(contentType); 
			response.setContentLength(new Long(file.length()).intValue());
			response.setHeader("Content-Disposition", "attachment; filename=" + name);
			ServletOutputStream outputStream = response.getOutputStream();
			is = new FileInputStream(file);
			buf = new BufferedInputStream(is);
			int readBytes = 0;
			//read from the file; write to the ServletOutputStream
			while ((readBytes = buf.read()) != -1) {
				outputStream.write(readBytes);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if ( buf != null) {
				buf.close();
			}
			if ( is != null ) {
				is.close();
			}
		}
	}
	
}
