package org.openforis.collect.web.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.web.session.SessionState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author S. Ricci
 * 
 */
@Controller
public class DataExportDownloadController extends BasicController {

	private static final String EXPORT_PATH = "export";

	private static Log LOG = LogFactory.getLog(DataExportDownloadController.class);
	
	@RequestMapping(value = "/downloadDataExport.htm", method = RequestMethod.GET)
	public @ResponseBody String downloadDataExport(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String path = buildDataExportFilePath(request);
			File file = new File(path);
			if ( file.exists() ) {
				writeFileToResponse(response, file);
			} else {
				throw new IOException("Data export file not found");
			}
		} catch (IOException e) {
			LOG.error(e);
			throw e;
		}
		return "ok";
	}
	
	private String buildDataExportFilePath(HttpServletRequest request) {
		ServletContext context = request.getSession().getServletContext();
		String exportRealPath = context.getRealPath(EXPORT_PATH);
		SessionState sessionState = getSessionState(request);
		String fileName = "data.zip";
		String sessionId = sessionState.getSessionId();
		StringBuilder sb = new StringBuilder();
		sb.append(exportRealPath).append(File.separator).append(sessionId).append(File.separator).append(fileName);
		return sb.toString();
	}

}
