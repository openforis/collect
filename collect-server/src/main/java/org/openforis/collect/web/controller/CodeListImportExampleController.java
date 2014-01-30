package org.openforis.collect.web.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author S. Ricci
 * 
 */
@Controller
public class CodeListImportExampleController extends BasicController {

	private static Log LOG = LogFactory.getLog(CodeListImportExampleController.class);
	
	private static final String ESAMPLE_CSV = "org/openforis/collect/designer/codelist/code-list-import-esample.csv";
	
	@RequestMapping(value = "/codelist/import/example.htm", method = RequestMethod.GET)
	public @ResponseBody String download(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			InputStream is = getClass().getResourceAsStream(ESAMPLE_CSV);
			IOUtils.copy(is, response.getOutputStream());
		} catch (IOException e) {
			LOG.error(e);
			throw e;
		}
		return "ok";
	}
	

}
