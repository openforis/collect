package org.openforis.collect.web.controller;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.dataexport.samplingdesign.SamplingDesignExportProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SamplingDesignController {

	private static final String CSV_CONTENT_TYPE = "text/csv";
	private static final String SAMPLING_DESIGN_CSV_FILE_NAME = "sampling_design.csv";
	
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	
	@RequestMapping(value = "/samplingdesign/export/work/{surveyId}", method = RequestMethod.GET)
	public @ResponseBody String exportWorkSamplingDesign(HttpServletResponse response,
			@PathVariable("surveyId") Integer surveyId) throws IOException {
		return exportSamplingDesign(response, surveyId, true);
	}
	
	@RequestMapping(value = "/samplingdesign/export/{surveyId}", method = RequestMethod.GET)
	public @ResponseBody String exportSamplingDesign(HttpServletResponse response,
			@PathVariable("surveyId") Integer surveyId) throws IOException {
		return exportSamplingDesign(response, surveyId, false);
	}
	
	protected String exportSamplingDesign(HttpServletResponse response,
			Integer surveyId, boolean work) throws IOException {
		SamplingDesignExportProcess process = new SamplingDesignExportProcess(samplingDesignManager);
		response.setContentType(CSV_CONTENT_TYPE); 
		String fileName = SAMPLING_DESIGN_CSV_FILE_NAME;
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		ServletOutputStream out = response.getOutputStream();
		process.exportToCSV(out, surveyId, work);
		return "ok";
	}
	
}
