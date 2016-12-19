package org.openforis.collect.io.metadata.collectearth;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.earth.app.server.AbstractPlacemarkDataController;
import org.openforis.collect.earth.app.server.PlacemarkUpdateRequest;
import org.openforis.collect.earth.app.service.EarthSurveyService;
import org.openforis.collect.io.metadata.collectearth.balloon.CollectEarthBalloonGenerator;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CollectEarthBalloonPreviewController extends AbstractPlacemarkDataController {
	
	@Autowired
	private EarthSurveyService earthSurveyService;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private SurveyManager surveyManager;

	@RequestMapping(value="/preview_placemark-info-expanded", method = GET)
	protected void placemarkInfoExpanded(@RequestParam("id") String placemarkId, HttpServletResponse response) throws IOException {
		CollectSurvey survey = sessionManager.getActiveDesignerSurvey();
		earthSurveyService.setCollectSurvey(survey);
		super.placemarkInfoExpanded(placemarkId, response);
	}

	@RequestMapping(value="/preview_save-data-expanded", method = POST)
	public void saveDataExpanded(PlacemarkUpdateRequest updateRequest, HttpServletResponse response) throws IOException {
		super.saveDataExpanded(updateRequest, response);
	}
	
	@RequestMapping(value = "/collectearthpreview.html", method = GET)
	public void showCollectEarthBalloonPreview(HttpServletResponse response, 
			@RequestParam("surveyId") Integer surveyId, @RequestParam("lang") String languageCode) throws IOException  {
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		CollectEarthBalloonGenerator generator = new CollectEarthBalloonGenerator(survey, languageCode, true);
		String html = generator.generateHTML();
		html = html.replace("earth.js", "earth_new.js");
		writeHtmlToResponse(response, html);
	}

	private void writeHtmlToResponse(HttpServletResponse response, String html) throws IOException {
		PrintWriter writer = null;
		try {
			response.setContentType(MediaType.TEXT_HTML_VALUE);
			writer = new PrintWriter(response.getOutputStream());
			writer.print(html);
			writer.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}
}
