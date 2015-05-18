package org.openforis.collect.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class Controllers {

	public static void writeFileToResponse(File file, String contentType, HttpServletResponse response,
			String outputFileName) throws FileNotFoundException, IOException {
		writeFileToResponse(new FileInputStream(file), contentType, new Long(file.length()).intValue(), response, outputFileName);
	}
	
	public static void writeFileToResponse(InputStream is,
			String contentType, int fileSize, HttpServletResponse response,
			String outputFileName) throws IOException {
		ServletOutputStream outputStream = response.getOutputStream();
		BufferedInputStream buf = null;
		try {
			response.setContentType(contentType); 
			response.setContentLength(fileSize);
			response.setHeader("Content-Disposition", "attachment; filename=" + outputFileName);
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
