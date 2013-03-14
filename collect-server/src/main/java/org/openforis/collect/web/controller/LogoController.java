package org.openforis.collect.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.manager.LogoManager;
import org.openforis.collect.model.Logo;
import org.openforis.collect.persistence.SurveyImportException;
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
public class LogoController {

	@Autowired
	private LogoManager logoManager;
	
	@RequestMapping(value = "/uploadLogo.htm", method = RequestMethod.POST)
	public @ResponseBody String uploadLogo(UploadItem uploadItem, BindingResult result, @RequestParam int position) 
			throws IOException, SurveyImportException, IdmlParseException {
		CommonsMultipartFile fileData = uploadItem.getFileData();
		InputStream is = fileData.getInputStream();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		IOUtils.copy(is, output);
		byte[] byteArray = output.toByteArray();
		Logo logo = new Logo(position, byteArray);
		logoManager.save(logo);
		return "ok";
	}
	
}
