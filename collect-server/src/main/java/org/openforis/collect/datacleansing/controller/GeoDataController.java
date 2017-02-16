package org.openforis.collect.datacleansing.controller;

import static org.openforis.collect.utils.Controllers.KML_CONTENT_TYPE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
public class GeoDataController {

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordManager recordManager;
	
	@RequestMapping(value = "survey/{surveyId}/data/coordinatevalues.json", method=GET)
	public @ResponseBody List<CoordinateAttributePoint> loadCoordinateValues(
			@PathVariable int surveyId, 
			@RequestParam int stepNum,
			@RequestParam int coordinateAttributeId, 
			@RequestParam int recordOffset, 
			@RequestParam int maxNumberOfRecords) {
		final List<CoordinateAttributePoint> result = new ArrayList<CoordinateAttributePoint>();
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		
		extractAllRecordCoordinates(survey, Step.valueOf(stepNum), recordOffset, maxNumberOfRecords, coordinateAttributeId, new CoordinateProcessor() {
			public void process(CollectRecord record, CoordinateAttribute coordAttr, Coordinate wgs84Coordinate) {
				CoordinateAttributePoint point = new CoordinateAttributePoint(coordAttr, wgs84Coordinate);
				result.add(point);
			}
		});
		return result;
	}

	@RequestMapping(value = "survey/{surveyId}/data/coordinatesvalues.kml", method=GET, produces=KML_CONTENT_TYPE)
	public void createCoordinateValuesKML(
			@PathVariable("surveyId") int surveyId, 
			@RequestParam int stepNum,
			@RequestParam int coordinateAttributeId,
			HttpServletResponse response) throws IOException {
		Kml kml = new Kml();
		
		CollectSurvey survey = surveyManager.getById(surveyId);
		
		final Document kmlDoc = kml.createAndSetDocument().withName(survey.getName());
		
		extractAllRecordCoordinates(survey, Step.valueOf(stepNum), null, null, coordinateAttributeId, new CoordinateProcessor() {
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
		
		public List<String> getRecordKeys() {
			CollectRecord record = (CollectRecord) this.attribute.getRecord();
			return record.getRootEntityKeyValues();
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
