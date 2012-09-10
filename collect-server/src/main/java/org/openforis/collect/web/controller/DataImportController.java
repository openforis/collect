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
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
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

	//private static Log LOG = LogFactory.getLog(DataImportController.class);

	private static final String EXPORT_PATH = "import";

	private static final String FILE_NAME = "data_import.zip";
	
	@RequestMapping(value = "/uploadData.htm", method = RequestMethod.POST)
	public @ResponseBody String uploadData(UploadItem uploadItem, BindingResult result, HttpServletRequest request, @RequestParam String sessionId) 
			throws IOException, InvalidIdmlException, SurveyImportException {
		File file = creteTempFile(request, sessionId);
		writeToFile(uploadItem, file);
		return "ok";
	}

	private File creteTempFile(HttpServletRequest request, String sessionId) throws IOException {
		HttpSession session = request.getSession();
		ServletContext servletContext = session.getServletContext();
		String importRealPath = servletContext.getRealPath(EXPORT_PATH);
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
	
	private void writeToFile(UploadItem uploadItem, File file) throws IOException {
		CommonsMultipartFile fileData = uploadItem.getFileData();
		InputStream is = fileData.getInputStream();
		OutputStream out=new FileOutputStream(file);
		byte buf[]=new byte[1024];
		int len;
		while ( ( len=is.read(buf) ) > 0 ) {
			out.write(buf,0,len);
		}
		out.close();
		is.close();
	}

}


