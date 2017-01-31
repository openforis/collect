package org.openforis.collect.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class Controllers {

	public static final String KML_CONTENT_TYPE = "application/vnd.google-earth.kml+xml";
	public static final String CSV_CONTENT_TYPE = "text/csv";

	public static void writeFileToResponse(HttpServletResponse response, File file) throws IOException {
		writeFileToResponse(response, file, file.getName());
	}
	
	public static void writeFileToResponse(HttpServletResponse response, File file, String outputFileName) throws FileNotFoundException, IOException {
		FileTypeMap defaultFileTypeMap = MimetypesFileTypeMap.getDefaultFileTypeMap();
		String contentType = defaultFileTypeMap.getContentType(outputFileName);
		writeFileToResponse(response, file, outputFileName, contentType);
	}
	
	public static void writeFileToResponse(HttpServletResponse response, File file, String outputFileName, 
			String contentType) throws FileNotFoundException, IOException {
		writeFileToResponse(response, new FileInputStream(file), outputFileName, contentType, new Long(file.length()).intValue());
	}
	
	public static void writeFileToResponse(HttpServletResponse response, InputStream is,
			String outputFileName, String contentType, int fileSize) throws IOException {
		BufferedInputStream buf = null;
		ServletOutputStream os = response.getOutputStream();
		try {
			setOutputContent(response, contentType, outputFileName, fileSize);
			buf = new BufferedInputStream(is);
			int readBytes = 0;
			//read from the file; write to the ServletOutputStream
			while ((readBytes = buf.read()) != -1) {
				os.write(readBytes);
			}
		} finally {
			IOUtils.closeQuietly(buf);
			IOUtils.closeQuietly(is);
			os.flush();
		}
	}

	public static void setOutputContent(HttpServletResponse response, String contentType, String outputFileName) {
		setOutputContent(response, contentType, outputFileName, null);
	}
	
	public static void setOutputContent(HttpServletResponse response, String contentType, String outputFileName, Integer contentLength) {
		response.setContentType(contentType); 
		response.setHeader("Content-Disposition", "attachment; filename=" + outputFileName);
		if (contentLength != null) {
			response.setContentLength(contentLength);
		}
	}
}
