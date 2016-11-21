package org.openforis.collect.web.controller;

import java.util.List;

import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.SamplingDesignItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SamplingPointsController {

	@Autowired
	private SamplingDesignManager samplingDesignManager;

	@RequestMapping(value = "surveys/{1}/sampling-points.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<SamplingDesignItem> loadSamplingPoints(@PathVariable int surveyId, 
			@RequestParam("parent_keys") String[] parentKeys) {
		return samplingDesignManager.loadChildItems(surveyId, parentKeys);
	}
	
}
