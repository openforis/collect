package org.openforis.collect.web.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.utils.Controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author S. Ricci
 * 
 */
@Controller
public class DataExportDownloadController extends BasicController {

	private static Log LOG = LogFactory.getLog(DataExportDownloadController.class);
	
	@RequestMapping(value = "/downloadDataExport.htm", method = RequestMethod.GET)
	public void downloadDataExport(
				HttpServletResponse response, 
				@RequestParam("fileName") String fileName, 
				@RequestParam("outputFileName") String outputFileName
			) throws IOException {
		try {
			File file = new File(fileName);
			Controllers.writeFileToResponse(response, file, outputFileName);
		} catch (IOException e) {
			LOG.error(e);
			throw e;
		}
	}
	
}
