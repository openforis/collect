package org.openforis.collect.web.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.utils.Controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author S. Ricci
 * 
 */
@Controller
public class ReferenceDataImportExampleDownloadController extends BasicController {

	private static final String IO_RESOURCES_ROOT_PATH = "WEB-INF/resources/io/";
	
	private static final String CODE_LIST_IMPORT_EXAMPLE_FILE_NAME = "code-list-import-example.csv";
	private static final String SPECIES_IMPORT_EXAMPLE_FILE_NAME = "species-list-import-example.csv";
	private static final String SAMPLING_DESIGN_IMPORT_EXAMPLE_FILE_NAME = "sampling-design-import-example.csv";
	
	@RequestMapping(value = "/codelist/import/example.htm", method = RequestMethod.GET)
	public void downloadCodeListImportExample(HttpServletRequest request, HttpServletResponse response) throws IOException {
		downloadFile(request, response, CODE_LIST_IMPORT_EXAMPLE_FILE_NAME);
	}

	@RequestMapping(value = "/species/import/example.htm", method = RequestMethod.GET)
	public void downloadSpeciesListImportExample(HttpServletRequest request, HttpServletResponse response) throws IOException {
		downloadFile(request, response, SPECIES_IMPORT_EXAMPLE_FILE_NAME);
	}
	
	@RequestMapping(value = "/samplingdesign/import/example.htm", method = RequestMethod.GET)
	public void downloadSamplingDesignImportExample(HttpServletRequest request, HttpServletResponse response) throws IOException {
		downloadFile(request, response, SAMPLING_DESIGN_IMPORT_EXAMPLE_FILE_NAME);
	}

	private void downloadFile(HttpServletRequest request,
			HttpServletResponse response, String fileName) throws IOException {
		ServletContext context = request.getSession().getServletContext();
		String path = context.getRealPath(IO_RESOURCES_ROOT_PATH + fileName);
		File file = new File(path);
		Controllers.writeFileToResponse(response, file);
	}
	

}
