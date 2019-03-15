package org.openforis.collect.web.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.manager.LogoManager;
import org.openforis.collect.model.Logo;
import org.openforis.collect.model.LogoPosition;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.web.controller.upload.UploadItem;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

@Controller
public class LogoController extends BasicController {

	private static final Logger LOG = LogManager.getLogger(LogoController.class);
	
	@Autowired
	private LogoManager logoManager;
	
	@RequestMapping(value = "/uploadLogo.htm", method = RequestMethod.POST)
	public @ResponseBody String uploadLogo(UploadItem uploadItem, BindingResult result, @RequestParam String position) 
			throws IOException, SurveyImportException, IdmlParseException {
		CommonsMultipartFile fileData = uploadItem.getFileData();
		InputStream is = fileData.getInputStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		IOUtils.copy(is, output);
		byte[] byteArray = output.toByteArray();
		String contentType = fileData.getContentType();
		LogoPosition p = LogoPosition.valueOf(position.toUpperCase(Locale.ENGLISH));
		Logo logo = new Logo(p, byteArray, contentType);
		logoManager.save(logo);
		return "ok";
	}
	
	@RequestMapping(value = "/downloadLogo.htm", method = RequestMethod.GET)
	public @ResponseBody void downloadLogo(HttpServletRequest request, HttpServletResponse response, @RequestParam String position) {
		LogoPosition p = LogoPosition.valueOf(position.toUpperCase(Locale.ENGLISH));
		Logo logo = logoManager.loadLogo(p);
		if ( logo != null ) {
			byte[] data = logo.getImage();
			ByteArrayInputStream is = new ByteArrayInputStream(data);
			try {
				String contentType = logo.getContentType();
				if ( contentType == null ) {
					contentType = "image/jpg";
				}
				//TODO get extension from db
				String extension = "jpg";
				Controllers.writeFileToResponse(response, is, "logo." + extension, contentType, data.length);
			} catch (IOException e) {
				LOG.error("Error writing logo in position: " + position, e);
			}
		}
	}

}
