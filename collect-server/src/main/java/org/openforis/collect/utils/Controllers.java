package org.openforis.collect.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.FileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class Controllers {

	public static void writeFileToResponse(HttpServletResponse response, File file) throws IOException {
		writeFileToResponse(response, file, file.getName());
	}
	
	public static void writeFileToResponse(HttpServletResponse response, File file, String outputFileName) throws FileNotFoundException, IOException {
		FileTypeMap defaultFileTypeMap = FileTypeMap.getDefaultFileTypeMap();
		String contentType = defaultFileTypeMap.getContentType(outputFileName);
		writeFileToResponse(response, file, outputFileName, contentType);
	}
	
	public static void writeFileToResponse(HttpServletResponse response, File file, String outputFileName, 
			String contentType) throws FileNotFoundException, IOException {
		writeFileToResponse(response, new FileInputStream(file), outputFileName, contentType, file.length());
	}
	
	public static void writeFileToResponse(HttpServletResponse response, InputStream is,
			String outputFileName, String contentType, long fileSize) throws IOException {
		ServletOutputStream os = response.getOutputStream();
		try {
			setOutputContent(response, outputFileName, contentType, fileSize);
			IOUtils.copy(is, os);
		} finally {
			IOUtils.closeQuietly(is);
			os.flush();
		}
	}

	public static void setOutputContent(HttpServletResponse response, String outputFileName, String contentType) {
		setOutputContent(response, outputFileName, contentType, null);
	}
	
	public static void setOutputContent(HttpServletResponse response, String outputFileName, String contentType, Long contentLength) {
		response.setContentType(contentType); 
		response.setHeader("Content-Disposition", "attachment; filename=\"" + outputFileName + "\"");
		if (contentLength != null) {
			if (contentLength <= Integer.MAX_VALUE) {
				response.setContentLength(contentLength.intValue());
			} else {
				response.setHeader("Content-length", contentLength.toString());
			}
		}
	}
	
}
