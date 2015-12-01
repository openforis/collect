package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.sql.SQLException;

import org.openforis.collect.event.RecordStep;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.RDBReportingRepositories;
import org.openforis.concurrency.ProgressListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
public class SaikuController {

	@Autowired
	private RDBReportingRepositories rdbReportingRepositories;
	
	@RequestMapping(value = "/saiku/datasources/{surveyName}/generate.json", method=GET)
	public @ResponseBody String generateRepository(@PathVariable("surveyName") String surveyName,
			@RequestParam("recordStep") RecordStep recordStep)
			throws CollectRdbException, SQLException {
		rdbReportingRepositories.createRepository(surveyName, recordStep, new ProgressListener() {
			public void progressMade() {
				System.out.println("progress made");
			}
		});
		return null;
	}

}
