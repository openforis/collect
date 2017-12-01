package org.openforis.collect.web.controller;

import static org.openforis.collect.utils.Controllers.CSV_CONTENT_TYPE;
import static org.openforis.collect.utils.Controllers.KML_CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.MultiPoint;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.dataexport.samplingdesign.SamplingDesignExportProcess;
import org.openforis.collect.metamodel.SamplingPointDataKmlGenerator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.collect.utils.Controllers;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.model.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SamplingPointsController extends BasicController {
	
	private static final String SAMPLING_DESIGN_CSV_FILE_NAME = "sampling_points.csv";
	
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private SurveyManager surveyManager;
	
	@RequestMapping(value="api/survey/{surveyId}/sampling_point_data", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<SamplingDesignItem> loadSamplingPoints(@PathVariable int surveyId, 
			@RequestParam(value="parent_keys", required=false) List<String> parentKeys, 
			@RequestParam(value="only_parent_item", required=false, defaultValue="false") boolean onlyParentItem) {
		if (parentKeys == null || parentKeys.isEmpty()) {
			return samplingDesignManager.loadChildItems(surveyId);
		} else if (onlyParentItem) {
			SamplingDesignItem item = samplingDesignManager.loadItem(surveyId, parentKeys);
			return Arrays.asList(item);
		} else {
			return samplingDesignManager.loadChildItems(surveyId, parentKeys);
		}
	}
	
	@RequestMapping(value = "api/survey/{surveyId}/sampling_point_data.csv", method=GET)
	public @ResponseBody String exportWorkSamplingDesign(HttpServletResponse response,
			@PathVariable("surveyId") Integer surveyId) throws IOException {
		SamplingDesignExportProcess process = new SamplingDesignExportProcess(samplingDesignManager);
		ServletOutputStream out = response.getOutputStream();
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		Controllers.setOutputContent(response, SAMPLING_DESIGN_CSV_FILE_NAME, CSV_CONTENT_TYPE);
		process.exportToCSV(out, survey);
		return "ok";
	}
	
	@RequestMapping(value="api/survey/{surveyId}/sampling_point_data", method=POST, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	SamplingDesignItem updateSamplingPointItem(@PathVariable int surveyId, @RequestBody SamplingDesignItem item) {
		samplingDesignManager.save(item);
		return item;
	}

	@RequestMapping(value = "api/survey/{surveyId}/sampling_point_data.kml", method=GET, produces=KML_CONTENT_TYPE)
	public void loadSamplingPointKmlData(@PathVariable int surveyId, HttpServletResponse response) throws Exception {
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		SamplingPointDataKmlGenerator samplingPointDataKmlGenerator = new SamplingPointDataKmlGenerator(samplingDesignManager, survey);
		samplingPointDataKmlGenerator.generate();
		samplingPointDataKmlGenerator.write(response.getOutputStream());
	}

	@RequestMapping(value = "api/survey/{surveyId}/sampling_point_data_features.json", method=GET)
	public @ResponseBody FeatureCollection loadSamplingPointData(@PathVariable int surveyId) {
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		return loadSamplingPointDataFeatures(survey);
	}

	private FeatureCollection loadSamplingPointDataFeatures(CollectSurvey survey) {
		FeatureCollection featureCollection = new FeatureCollection();
		Feature feature = new Feature();
		feature.setProperty("letter", "o");
		feature.setProperty("color", "blue");
		feature.setProperty("rank", "15");
		MultiPoint multiPoint = new MultiPoint();
		
		CoordinateOperations coordinateOperations = getCoordinateOperations(survey);
		List<SamplingDesignItem> samplingDesignItems = loadSamplingDesignItems(survey);
		for (SamplingDesignItem item : samplingDesignItems) {
			Coordinate coordinate = new Coordinate(item.getX(), item.getY(), item.getSrsId());
			multiPoint.add(createLngLatAlt(coordinateOperations, coordinate));
		}
		feature.setGeometry(multiPoint);
		featureCollection.add(feature);
		return featureCollection;
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
	
	private CoordinateOperations getCoordinateOperations(CollectSurvey survey) {
		CollectSurveyContext surveyContext = survey.getContext();
		return surveyContext.getCoordinateOperations();
	}
	
	@RequestMapping(value = "survey/{surveyId}/sampling_point_bounds.json", method=GET)
	public @ResponseBody Bounds loadSamplingPointBounds(@PathVariable int surveyId) {
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);

		CollectSurveyContext surveyContext = survey.getContext();
		CoordinateOperations coordinateOperations = surveyContext.getCoordinateOperations();

		SamplingDesignSummaries samplingDesignSummaries = samplingDesignManager.loadBySurvey(survey.getId());
		List<SamplingDesignItem> samplingDesignItems = samplingDesignSummaries.getRecords();
		Bounds bounds = new Bounds();
		for (SamplingDesignItem item : samplingDesignItems) {
			Coordinate coordinate = new Coordinate(item.getX(), item.getY(), item.getSrsId());
			LngLatAlt lngLatAlt = createLngLatAlt(coordinateOperations, coordinate);
			if (lngLatAlt != null) {
				if (bounds.topLeft == null) {
					bounds.topLeft = bounds.topRight = bounds.bottomLeft = bounds.bottomRight = lngLatAlt;
				} else {
					if (lngLatAlt.getLatitude() < bounds.topLeft.getLatitude() && 
						lngLatAlt.getLongitude() < bounds.topLeft.getLongitude()) {
						bounds.topLeft = lngLatAlt;
					} else if (lngLatAlt.getLatitude() < bounds.topRight.getLatitude() && 
						lngLatAlt.getLongitude() > bounds.topRight.getLongitude()) {
						bounds.topRight = lngLatAlt;
					} else if (lngLatAlt.getLatitude() > bounds.bottomRight.getLatitude() && 
						lngLatAlt.getLongitude() > bounds.bottomRight.getLongitude()) {
						bounds.bottomRight = lngLatAlt;
					} else if (lngLatAlt.getLatitude() > bounds.bottomLeft.getLatitude() && 
						lngLatAlt.getLongitude() > bounds.bottomLeft.getLongitude()) {
						bounds.bottomLeft = lngLatAlt;
					}
				}
			}
		}
		return bounds;
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
	
	private static class Bounds {
		
		private LngLatAlt topRight;
		private LngLatAlt topLeft;
		private LngLatAlt bottomRight;
		private LngLatAlt bottomLeft;
		
	}
	
	public static class SamplingPointSearchParameters {
		
		private List<String> parentKeys;
		private boolean onlyParentItem;
		
		public List<String> getParentKeys() {
			return parentKeys;
		}
		
		public void setParentKeys(List<String> parentKeys) {
			this.parentKeys = parentKeys;
		}

		public boolean isOnlyParentItem() {
			return onlyParentItem;
		}
		
		public void setOnlyParentItem(boolean onlyParentItem) {
			this.onlyParentItem = onlyParentItem;
		}
	}
}
