package org.openforis.collect.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.geojson.LngLatAlt;
import org.jooq.tools.StringUtils;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.dataexport.samplingdesign.SamplingDesignExportProcess;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.model.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;

@Controller
public class SamplingDesignController {

	private static final String CSV_CONTENT_TYPE = "text/csv";
	private static final String KML_CONTENT_TYPE = "application/vnd.google-earth.kml+xml";
	private static final String SAMPLING_DESIGN_CSV_FILE_NAME = "sampling_design.csv";
	
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private SurveyManager surveyManager;
	
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
	
	@RequestMapping(value = "/survey/{surveyId}/samplingpointdata.kml", method = RequestMethod.GET, produces = KML_CONTENT_TYPE)
	public void loadSamplingPointKmlData(@PathVariable int surveyId, HttpServletResponse response) throws Exception {
		Kml kml = KmlFactory.createKml();
		Document doc = kml.createAndSetDocument();
		CollectSurvey survey = surveyManager.getById(surveyId);
		CoordinateOperations coordinateOperations = survey.getContext().getCoordinateOperations();
		List<SamplingDesignItem> samplingDesignItems = loadSamplingDesignItems(survey);
		for (SamplingDesignItem item : samplingDesignItems) {
			Coordinate coordinate = new Coordinate(item.getX(), item.getY(), item.getSrsId());
			LngLatAlt lngLatAlt = createLngLatAlt(coordinateOperations, coordinate);
			String name = StringUtils.join(item.getLevelCodes().toArray(new String[item.getLevelCodes().size()]), '|');
			doc.createAndAddPlacemark()
					.withName(name)
					.withOpen(true)
					.createAndSetPoint()
					.addToCoordinates(lngLatAlt.getLongitude(), lngLatAlt.getLatitude());
		}
		kml.marshal(response.getOutputStream());
	}
	
	private List<SamplingDesignItem> loadSamplingDesignItems(CollectSurvey survey) {
		SamplingDesignSummaries samplingDesignSummaries = samplingDesignManager.loadBySurvey(survey.getId());
		List<SamplingDesignItem> samplingDesignItems = samplingDesignSummaries.getRecords();
		List<SamplingDesignItem> result = new ArrayList<SamplingDesignItem>();
		for (SamplingDesignItem item : samplingDesignItems) {
			result.add(item);
		}
		return result;
	}
	
	private LngLatAlt createLngLatAlt(CoordinateOperations coordOpts, Coordinate coord) {
		try {
			Coordinate wgs84Coord = coordOpts.convertToWgs84(coord);
			LngLatAlt lngLatAlt = new LngLatAlt(wgs84Coord.getX(), wgs84Coord.getY());
			return lngLatAlt;
		} catch(Exception e) {
			return null;
		}
	}

	protected String exportSamplingDesign(HttpServletResponse response,
			Integer surveyId, boolean work) throws IOException {
		SamplingDesignExportProcess process = new SamplingDesignExportProcess(samplingDesignManager);
		response.setContentType(CSV_CONTENT_TYPE); 
		String fileName = SAMPLING_DESIGN_CSV_FILE_NAME;
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		ServletOutputStream out = response.getOutputStream();
		CollectSurvey survey = work ? surveyManager.loadSurvey(surveyId): surveyManager.getById(surveyId);
		process.exportToCSV(out, survey);
		return "ok";
	}
	
}
