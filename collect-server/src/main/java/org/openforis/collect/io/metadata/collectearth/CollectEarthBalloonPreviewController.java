package org.openforis.collect.io.metadata.collectearth;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.earth.app.server.AbstractPlacemarkDataController;
import org.openforis.collect.earth.app.server.PlacemarkEntityCreateParams;
import org.openforis.collect.earth.app.server.PlacemarkUpdateRequest;
import org.openforis.collect.io.metadata.collectearth.balloon.CollectEarthBalloonGenerator;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CollectEarthBalloonPreviewController extends AbstractPlacemarkDataController {

	@Autowired
	@Qualifier("sessionManager")
	private SessionManager sessionManager;
	@Autowired
	private SurveyManager surveyManager;

	@Override
	@GetMapping(value="/preview_placemark-info-expanded")
	public void placemarkInfoExpanded(@RequestParam("id") String placemarkId, HttpServletResponse response) throws IOException {
		CollectSurvey survey = sessionManager.getActiveDesignerSurvey();
		earthSurveyService.setCollectSurvey(survey);
		super.placemarkInfoExpanded(placemarkId, response);
	}

	@Override
	@PostMapping(value="/preview_save-data-expanded")
	public void saveDataExpanded(PlacemarkUpdateRequest updateRequest, HttpServletResponse response) throws IOException {
		super.saveDataExpanded(updateRequest, response);
	}
	
	@Override
	@PostMapping(value="/preview_create-entity")
	public void createEntity(PlacemarkEntityCreateParams params, HttpServletResponse response) throws IOException {
		super.createEntity(params, response);
	}
	
	@Override
	@DeleteMapping(value="/preview_delete-entity")
	public void deleteEntity(PlacemarkEntityCreateParams params, HttpServletResponse response) throws IOException {
		super.deleteEntity(params, response);
	}


	@GetMapping(value = "/collectearthpreview.html")
	public void showCollectEarthBalloonPreview(HttpServletResponse response,
			@RequestParam("surveyId") Integer surveyId, @RequestParam("lang") String languageCode) throws IOException  {
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		CollectEarthBalloonGenerator generator = new CollectEarthBalloonGenerator(survey, languageCode, true);
		String html = generator.generateHTML();
		html = html.replace("earth.js", "earth_new.js");
		writeHtmlToResponse(response, html);
	}

	private void writeHtmlToResponse(HttpServletResponse response, String html) throws IOException {
		try ( PrintWriter writer = new PrintWriter(response.getOutputStream()) ){
			response.setContentType(MediaType.TEXT_HTML_VALUE);
			writer.print(html);
			writer.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
