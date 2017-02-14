package org.openforis.collect.datacleansing.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.MultiPoint;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.collect.model.proxy.SamplingDesignItemProxy;
import org.openforis.collect.utils.Proxies;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/geo/data/")
public class GeoDataController {

	private static final String KML_CONTENT_TYPE = "application/vnd.google-earth.kml+xml";
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	
	@RequestMapping(value = "samplingpoints.json", method = RequestMethod.GET)
	public @ResponseBody FeatureCollection loadSamplingPointData() {
		CollectSurvey survey = sessionManager.getActiveSurvey();
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

	@RequestMapping(value = "samplingpoints.kml", method = RequestMethod.GET, produces = KML_CONTENT_TYPE)
	public void loadSamplingPointKmlData(HttpServletResponse response) throws Exception {
		Kml kml = KmlFactory.createKml();
		Document doc = kml.createAndSetDocument();
		CollectSurvey survey = sessionManager.getActiveSurvey();
		CoordinateOperations coordinateOperations = getCoordinateOperations(survey);
		List<SamplingDesignItem> samplingDesignItems = loadSamplingDesignItems(survey);
		for (SamplingDesignItem item : samplingDesignItems) {
			Coordinate coordinate = new Coordinate(item.getX(), item.getY(), item.getSrsId());
			LngLatAlt lngLatAlt = createLngLatAlt(coordinateOperations, coordinate);
			doc.createAndAddPlacemark().withName(item.getLevelCode(1)).withOpen(true).createAndSetPoint()
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
	
	private CoordinateOperations getCoordinateOperations(CollectSurvey survey) {
		CollectSurveyContext surveyContext = survey.getContext();
		return surveyContext.getCoordinateOperations();
	}
	
	@RequestMapping(value = "samplingpointbounds.json", method = RequestMethod.GET)
	public @ResponseBody Bounds loadSamplingPointBounds() {
		CollectSurvey survey = sessionManager.getActiveSurvey();
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
	
	@RequestMapping(value = "{surveyName}/samplingpointcoordinates.json", method = RequestMethod.GET)
	public @ResponseBody List<SamplingDesignItemProxy> loadSamplingCoordinates(
			@PathVariable("surveyName") String surveyName, 
			int recordOffset, int maxNumberOfRecords) {
		CollectSurvey survey = surveyManager.get(surveyName);
		return loadSamplingPointDataItems(survey);
	}
	
	private List<SamplingDesignItemProxy> loadSamplingPointDataItems(CollectSurvey survey) {
		return Proxies.fromList(loadSamplingDesignItems(survey), SamplingDesignItemProxy.class);
	}
	
//	@RequestMapping(value = "{surveyName}/{coordinateAttributeId}/coordinatevalues.json", method = RequestMethod.GET)
//	public @ResponseBody List<LngLatAlt> loadCoordinateValues(
//			@PathVariable("surveyName") String surveyName, 
//			@PathVariable("step") int stepNumber,
//			@PathVariable("coordinateAttributeId") int coordinateAttributeId, 
//			int recordOffset, int maxNumberOfRecords) {
//		final List<LngLatAlt> result = new ArrayList<LngLatAlt>();
//		CollectSurvey survey = surveyManager.get(surveyName);
//		
//		extractAllRecordCoordinates(survey, Step.valueOf(stepNumber), recordOffset, maxNumberOfRecords, coordinateAttributeId, new CoordinateProcessor() {
//			public void process(CollectRecord record, CoordinateAttribute coordAttr, Coordinate wgs84Coordinate) {
//				LngLatAlt lngLatAlt = new LngLatAlt(wgs84Coordinate.getX(), wgs84Coordinate.getY());
//				result.add(lngLatAlt);
//			}
//		});
//		return result;
//	}
	
	@RequestMapping(value = "{surveyName}/{step}/{coordinateAttributeId}/coordinatevalues.json", method = RequestMethod.GET)
	public @ResponseBody List<CoordinateAttributePoint> loadCoordinateValues(
			@PathVariable("surveyName") String surveyName, 
			@PathVariable("step") int stepNumber,
			@PathVariable("coordinateAttributeId") int coordinateAttributeId, 
			int recordOffset, int maxNumberOfRecords) {
		final List<CoordinateAttributePoint> result = new ArrayList<CoordinateAttributePoint>();
		CollectSurvey survey = surveyManager.get(surveyName);
		
		extractAllRecordCoordinates(survey, Step.valueOf(stepNumber), recordOffset, maxNumberOfRecords, coordinateAttributeId, new CoordinateProcessor() {
			public void process(CollectRecord record, CoordinateAttribute coordAttr, Coordinate wgs84Coordinate) {
				CoordinateAttributePoint point = new CoordinateAttributePoint(coordAttr, wgs84Coordinate);
				result.add(point);
			}
		});
		return result;
	}

	@RequestMapping(value = "{surveyName}/{coordinateAttributeId}/coordinates.kml", method = RequestMethod.GET, produces = KML_CONTENT_TYPE)
	public void createCoordinateValuesKML(
			@PathVariable("surveyName") String surveyName, 
			@PathVariable("coordinateAttributeId") int coordinateAttributeId, 
			@PathVariable("step") int stepNumber,
			HttpServletResponse response) throws IOException {
		Kml kml = new Kml();
		
		CollectSurvey survey = surveyManager.get(surveyName);
		
		final Document kmlDoc = kml.createAndSetDocument().withName(surveyName);
		
		extractAllRecordCoordinates(survey, Step.valueOf(stepNumber), null, null, coordinateAttributeId, new CoordinateProcessor() {
			public void process(CollectRecord record, CoordinateAttribute coordAttr, Coordinate wgs84Coordinate) {
				kmlDoc.createAndAddPlacemark()
					.withName(record.getRootEntityKeyValues().toString())
					.withOpen(Boolean.TRUE)
					.createAndSetPoint()
					.addToCoordinates(wgs84Coordinate.getY(), wgs84Coordinate.getX());
			}
		});
		kml.marshal(response.getOutputStream());
	}
	
	private void extractAllRecordCoordinates(CollectSurvey survey, Step step, Integer recordOffset, Integer maxNumberOfRecords, 
			int coordinateAttributeId, CoordinateProcessor coordinateProcessor) {
		CoordinateOperations coordinateOperations = survey.getContext().getCoordinateOperations();
		CoordinateAttributeDefinition coordAttrDef = (CoordinateAttributeDefinition) 
				survey.getSchema().getDefinitionById(coordinateAttributeId);

		RecordFilter filter = new RecordFilter(survey);
		filter.setStepGreaterOrEqual(step);
		filter.setOffset(recordOffset);
		filter.setMaxNumberOfRecords(maxNumberOfRecords);
		List<CollectRecord> summaries = recordManager.loadSummaries(filter);
		for (CollectRecord summary : summaries) {
			CollectRecord record = recordManager.load(survey, summary.getId(), step, false);
			List<Node<?>> nodes = record.findNodesByPath(coordAttrDef.getPath());
			for (Node<?> node : nodes) {
				CoordinateAttribute coordAttr = (CoordinateAttribute) node;
				if (coordAttr.isFilled()) {
					Coordinate coordinate = coordAttr.getValue();
					Coordinate wgs84Coord = coordinateOperations.convertToWgs84(coordinate);
					coordinateProcessor.process(record, coordAttr, wgs84Coord);
				}
			}
		}
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
	
	public static class CoordinateAttributePoint {
		
		private CoordinateAttribute attribute;
		private Coordinate latLongCoordinate;

		public CoordinateAttributePoint(CoordinateAttribute attribute, Coordinate latLongCoordinate) {
			this.attribute = attribute;
			this.latLongCoordinate = latLongCoordinate;
		}
		
		public int getRecordId() {
			return attribute.getRecord().getId();
		}
		
		public int getAttributeId() {
			return attribute.getInternalId();
		}
		
		public int getAttributeDefinitionId() {
			return attribute.getDefinition().getId();
		}
		
		public Double getLat() {
			return latLongCoordinate == null ? null : latLongCoordinate.getY();
		}
		
		public Double getLon() {
			return latLongCoordinate == null ? null : latLongCoordinate.getX();
		}
		
		public String[] getRecordKeys() {
			return this.attribute.getRecord().getRootEntity().getKeyValues();
		}
		
		public Double getDistanceToExpectedLocation() {
			DistanceCheck distanceCheck = this.attribute.getDefinition().extractMaxDistanceCheck();
			if (distanceCheck == null) {
				return null;
			} else {
				return distanceCheck.evaluateDistanceToDestination(attribute);
			}
		}
		
	}
	
	private interface CoordinateProcessor {
		
		void process(CollectRecord record, CoordinateAttribute coordAttr, Coordinate wgs84Coordinate);
	}
}
