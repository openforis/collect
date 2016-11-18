package org.openforis.collect.web.controller;

import java.util.List;

import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.SurveySummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/samplingpoints/")
public class SamplingPointsController {

	@Autowired
	private SamplingDesignManager samplingDesignManager;

	@RequestMapping(value = "summaries.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<SurveySummary> loadSummaries(
	
}
