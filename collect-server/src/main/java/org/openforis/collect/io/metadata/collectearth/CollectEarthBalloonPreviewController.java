package org.openforis.collect.io.metadata.collectearth;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.earth.app.server.AbstractPlacemarkDataController;
import org.openforis.collect.earth.app.server.PlacemarkUpdateRequest;
import org.openforis.collect.earth.app.service.EarthSurveyService;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CollectEarthBalloonPreviewController extends AbstractPlacemarkDataController {
	
	@Autowired
	private EarthSurveyService earthSurveyService;
	@Autowired
	private SessionManager sessionManager;

	@RequestMapping(value="/preview_placemark-info-expanded", method = RequestMethod.GET)
	protected void placemarkInfoExpanded(@RequestParam("id") String placemarkId, HttpServletResponse response) throws IOException {
		CollectSurvey survey = sessionManager.getActiveDesignerSurvey();
		earthSurveyService.setCollectSurvey(survey);
		super.placemarkInfoExpanded(placemarkId, response);
	}

	@RequestMapping(value="/preview_save-data-expanded", method = RequestMethod.POST)
	public void saveDataExpanded(PlacemarkUpdateRequest updateRequest, HttpServletResponse response) throws IOException {
		super.saveDataExpanded(updateRequest, response);
	}
}
