package org.openforis.collect.web.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.web.controller.upload.UploadItem;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * 
 * Handles files upload into the "temp" folder (relative to the context path).
 * The files are uploaded in a subfolder named #sessionid for each http session.
 * 
 * 
 * @author S. Ricci
 * 
 */
@Controller
public class FileUploadController {

	//private static Log LOG = LogFactory.getLog(FileUploadController.class);

	public static final String TEMP_PATH = "temp";

	@RequestMapping(value = "/uploadFile.htm", method = RequestMethod.POST)
	public @ResponseBody String uploadData(UploadItem uploadItem, BindingResult result, HttpServletRequest request, @RequestParam String sessionId) 
			throws IOException, SurveyImportException {
		File file = creteTempFile(request, sessionId, uploadItem.getName());
		writeToFile(uploadItem, file);
		return "ok";
	}

	protected File creteTempFile(HttpServletRequest request, String sessionId, String fileName) throws IOException {
		HttpSession session = request.getSession();
		ServletContext servletContext = session.getServletContext();
		String importRealPath = servletContext.getRealPath(TEMP_PATH);
		File tempRootDirectory = new File(importRealPath);
		File sessionTempDirectory = getSessionTempDirectory(tempRootDirectory, sessionId);
		File file = new File(sessionTempDirectory, fileName);
		if ( file.exists() ) {
			file.delete();
		}
		file.createNewFile();
		return file;
	}
	
	public static File getSessionTempDirectory(File tempRootDirectory, String sessionId) {
		File sessionTempDirectory = new File(tempRootDirectory, sessionId);
		if ( ! sessionTempDirectory.exists() ) {
			sessionTempDirectory.mkdirs();
		} 
		return sessionTempDirectory;
	}
	
	protected void writeToFile(UploadItem uploadItem, File file) throws IOException {
		CommonsMultipartFile fileData = uploadItem.getFileData();
		InputStream is = fileData.getInputStream();
		OutputStream out = new FileOutputStream(file);
		byte buf[] = new byte[1024];
		int len;
		while ( ( len=is.read(buf) ) > 0 ) {
			out.write(buf,0,len);
		}
		out.close();
		is.close();
	}

}


