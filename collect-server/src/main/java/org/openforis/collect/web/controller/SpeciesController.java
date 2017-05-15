package org.openforis.collect.web.controller;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.dataexport.species.SpeciesExportProcess;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SpeciesController {

	private static final String SPECIES_LIST_CSV_FILE_NAME = "species_list.csv";
	
	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	private SurveyManager surveyManager;
	
	@RequestMapping(value = "/survey/{surveyId}/taxonomy/{taxonomyId}/export.csv", method = RequestMethod.GET)
	public @ResponseBody String exportSpecies(HttpServletResponse response,
			@PathVariable("surveyId") Integer surveyId,
			@PathVariable("taxonomyId") Integer taxonomyId) throws IOException {
		response.setHeader("Content-Disposition", "attachment; filename=" + SPECIES_LIST_CSV_FILE_NAME);
		response.setContentType("text/csv"); 
		ServletOutputStream out = response.getOutputStream();
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		SpeciesExportProcess process = new SpeciesExportProcess(speciesManager);
		process.exportToCSV(out, survey, taxonomyId);
		return "ok";
	}
	
}
