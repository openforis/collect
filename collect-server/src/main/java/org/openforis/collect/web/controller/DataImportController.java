package org.openforis.collect.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * @author S. Ricci
 * 
 */
@Controller
public class DataImportController {

	private static Log LOG = LogFactory.getLog(DataImportController.class);

	private static final String IMPORT_PATH = "import";

	private static final String FILE_NAME = "data_import.zip";
	
	@RequestMapping(value = "/uploadData.htm", method = RequestMethod.POST)
	public @ResponseBody String uploadData(UploadItem uploadItem, BindingResult result, HttpServletRequest request, @RequestParam String sessionId) 
			throws IOException, SurveyImportException {
		LOG.info("Uploading data file...");
		File file = creteTempFile(request, sessionId);
		
		LOG.info("Writing file: " + file.getAbsolutePath());
		
		//copy upload item to temp file
		CommonsMultipartFile fileData = uploadItem.getFileData();
		InputStream is = fileData.getInputStream();
		FileUtils.copyInputStreamToFile(is, file);
		
		LOG.info("Data file succeffully written");
		return "ok";
	}

	private File creteTempFile(HttpServletRequest request, String sessionId) throws IOException {
		HttpSession session = request.getSession();
		ServletContext servletContext = session.getServletContext();
		String importRealPath = servletContext.getRealPath(IMPORT_PATH);
		File importRootDirectory = new File(importRealPath);
		File importDirectory = new File(importRootDirectory, sessionId);
		if ( ! importDirectory.exists() ) {
			importDirectory.mkdirs();
		} 
		File file = new File(importDirectory, FILE_NAME);
		if ( file.exists() ) {
			file.delete();
		}
		file.createNewFile();
		return file;
	}

}
