package org.openforis.collect.web.controller;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.dataexport.species.SpeciesExportProcess;
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
	
	@RequestMapping(value = "/species/export/{taxonomyId}", method = RequestMethod.GET)
	public @ResponseBody String exportSpecies(HttpServletResponse response,
			@PathVariable("taxonomyId") Integer taxonomyId) throws IOException {
		SpeciesExportProcess process = new SpeciesExportProcess(speciesManager);
		response.setContentType("text/csv"); 
		String fileName = SPECIES_LIST_CSV_FILE_NAME;
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		ServletOutputStream out = response.getOutputStream();
		process.exportToCSV(out, taxonomyId);
		return "ok";
	}
	
}
