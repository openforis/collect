package org.openforis.collect.web.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.dataexport.samplingdesign.SamplingDesignExportProcess;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.MediaType.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.springframework.web.bind.annotation.RequestMethod.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SamplingPointsController {
	
	private static final String CSV_CONTENT_TYPE = "text/csv";
	private static final String SAMPLING_DESIGN_CSV_FILE_NAME = "sampling_points.csv";
	
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private SurveyManager surveyManager;
	
	@RequestMapping(value="survey/{surveyId}/sampling-point-data/list.json", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<SamplingDesignItem> loadSamplingPoints(@PathVariable int surveyId, 
			@RequestParam("parent_keys") String[] parentKeys) {
		return samplingDesignManager.loadChildItems(surveyId, parentKeys);
	}
	
	@RequestMapping(value = "survey/{surveyId}/sampling-point-data/export.csv", method = RequestMethod.GET)
	public @ResponseBody String exportWorkSamplingDesign(HttpServletResponse response,
			@PathVariable("surveyId") Integer surveyId) throws IOException {
		SamplingDesignExportProcess process = new SamplingDesignExportProcess(samplingDesignManager);
		response.setContentType(CSV_CONTENT_TYPE); 
		String fileName = SAMPLING_DESIGN_CSV_FILE_NAME;
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		ServletOutputStream out = response.getOutputStream();
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		process.exportToCSV(out, survey);
		return "ok";
	}
}
