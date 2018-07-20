package org.openforis.collect.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.web.controller.upload.UploadItem;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	//private static Logger LOG = Logger.getLogger(FileUploadController.class);

	public static final String TEMP_PATH = "temp";

	@RequestMapping(value = "/uploadFile.htm", method = RequestMethod.POST)
	public @ResponseBody String uploadFile(UploadItem uploadItem) 
			throws IOException, SurveyImportException {
		CommonsMultipartFile fileData = uploadItem.getFileData();

		File file = File.createTempFile("collect_", fileData.getOriginalFilename());
		
		InputStream is = fileData.getInputStream();

		FileUtils.copyInputStreamToFile(is, file);
		
		return file.getAbsolutePath();
	}
	
}
