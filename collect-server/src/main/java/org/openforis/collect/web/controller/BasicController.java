package org.openforis.collect.web.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.web.session.SessionState;

/**
 * @author S. Ricci
 * 
 */
public abstract class BasicController {

	protected SessionState getSessionState(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if(session != null) {
			SessionState sessionState = (SessionState) session.getAttribute(SessionState.SESSION_ATTRIBUTE_NAME);
			return sessionState;
		}
		return null;
	}
	
	protected void writeFileToResponse(HttpServletResponse response, File file) throws IOException {
		writeFileToResponse(new FileInputStream(file), file.getName(), Long.valueOf(file.length()).intValue(), response);
	}
	
	protected void writeFileToResponse(HttpServletResponse response, File file, String outputFileName) throws IOException {
		writeFileToResponse(new FileInputStream(file), outputFileName, Long.valueOf(file.length()).intValue(), response);	
	}
	
	protected void writeFileToResponse(InputStream is, String outputFileName, int fileSize, HttpServletResponse response) throws IOException {
		FileTypeMap defaultFileTypeMap = MimetypesFileTypeMap.getDefaultFileTypeMap();
		String contentType = defaultFileTypeMap.getContentType(outputFileName);
		writeFileToResponse(is, contentType, fileSize, response,
				outputFileName);
	}

	protected void writeFileToResponse(File file,
			String contentType, int fileSize, HttpServletResponse response,
			String outputFileName) throws IOException {
		writeFileToResponse(new FileInputStream(file), contentType, fileSize, response, outputFileName);
	}
	
	protected void writeFileToResponse(InputStream is,
			String contentType, int fileSize, HttpServletResponse response,
			String outputFileName) throws IOException {
		BufferedInputStream buf = null;
		try {
			response.setContentType(contentType); 
			response.setContentLength(fileSize);
			response.setHeader("Content-Disposition", "attachment; filename=" + outputFileName);
			ServletOutputStream outputStream = response.getOutputStream();
			buf = new BufferedInputStream(is);
			int readBytes = 0;
			//read from the file; write to the ServletOutputStream
			while ((readBytes = buf.read()) != -1) {
				outputStream.write(readBytes);
			}
		} finally {
			IOUtils.closeQuietly(buf);
			IOUtils.closeQuietly(is);
		}
	}
	
}
