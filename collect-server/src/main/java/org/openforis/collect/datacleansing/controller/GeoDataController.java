package org.openforis.collect.datacleansing.controller;

import java.util.ArrayList;
import java.util.List;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.MultiPoint;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/geo/data/")
public class GeoDataController {

	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	
	@RequestMapping(value = "samplingpoints.json", method = RequestMethod.GET)
	public @ResponseBody FeatureCollection loadSamplingPointData() {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		CollectSurveyContext surveyContext = survey.getContext();
		CoordinateOperations coordinateOperations = surveyContext.getCoordinateOperations();

		FeatureCollection featureCollection = new FeatureCollection();
		Feature feature = new Feature();
		feature.setProperty("letter", "o");
		feature.setProperty("color", "blue");
		feature.setProperty("rank", "15");
		MultiPoint multiPoint = new MultiPoint();

		SamplingDesignSummaries samplingDesignSummaries = samplingDesignManager.loadBySurvey(survey.getId());
		List<SamplingDesignItem> samplingDesignItems = samplingDesignSummaries.getRecords();
		for (SamplingDesignItem item : samplingDesignItems) {
			Coordinate coordinate = new Coordinate(item.getX(), item.getY(), item.getSrsId());
			multiPoint.add(createLngLatAlt(coordinateOperations, coordinate));
		}
		feature.setGeometry(multiPoint);
		featureCollection.add(feature);
		return featureCollection;
	}

	@RequestMapping(value = "coordinatevalues.json", method = RequestMethod.GET)
	public @ResponseBody List<LngLatAlt> loadCoordinateValues(int recordOffset, int maxNumberOfRecords, int coordinateAttributeId) {
		List<LngLatAlt> result = new ArrayList<LngLatAlt>();
		CollectSurvey survey = sessionManager.getActiveSurvey();
		CoordinateOperations coordinateOperations = survey.getContext().getCoordinateOperations();
		CoordinateAttributeDefinition coordAttrDef = (CoordinateAttributeDefinition) survey.getSchema().getDefinitionById(coordinateAttributeId);

		RecordFilter filter = new RecordFilter(survey);
		filter.setOffset(recordOffset);
		filter.setMaxNumberOfRecords(maxNumberOfRecords);
		List<CollectRecord> summaries = recordManager.loadSummaries(filter);
		for (CollectRecord record : summaries) {
			List<Node<?>> nodes = record.findNodesByPath(coordAttrDef.getPath());
			for (Node<?> node : nodes) {
				CoordinateAttribute coordAttr = (CoordinateAttribute) node;
				Coordinate coordinate = coordAttr.getValue();
				result.add(createLngLatAlt(coordinateOperations, coordinate));
			}
		}
		return result;
	}

	private LngLatAlt createLngLatAlt(CoordinateOperations coordOpts, Coordinate coord) {
		Coordinate wgs84Coord = coordOpts.convertToWgs84(coord);
		LngLatAlt lngLatAlt = new LngLatAlt(wgs84Coord.getX(), wgs84Coord.getY());
		return lngLatAlt;
	}
}
