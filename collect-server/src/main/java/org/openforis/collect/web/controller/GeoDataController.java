package org.openforis.collect.web.controller;

import static org.openforis.collect.utils.Controllers.KML_CONTENT_TYPE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.geospatial.GeoToolsCoordinateOperations;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
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
			@RequestParam int coordinateAttributeId,
			@RequestParam String srsId,
			@RequestParam int recordOffset, 
			@RequestParam int maxNumberOfRecords) {
		final List<CoordinateAttributePoint> result = new ArrayList<CoordinateAttributePoint>();
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		
		extractAllRecordCoordinates(survey, recordOffset, maxNumberOfRecords, 
				coordinateAttributeId, srsId, new CoordinateProcessor() {
			public void process(CollectRecord record, CoordinateAttribute coordAttr, Coordinate coordinate) {
				CoordinateAttributePoint point = new CoordinateAttributePoint(coordAttr, coordinate);
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
		
		extractAllRecordCoordinates(survey, null, null, coordinateAttributeId, 
				SpatialReferenceSystem.WGS84_SRS_ID, new CoordinateProcessor() {
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
	
	private void extractAllRecordCoordinates(CollectSurvey survey, Integer recordOffset, Integer maxNumberOfRecords, 
			int coordinateAttributeId, String toSrsId, CoordinateProcessor coordinateProcessor) {
		GeoToolsCoordinateOperations coordinateOperations = new GeoToolsCoordinateOperations();
		coordinateOperations.registerSRS(survey.getSpatialReferenceSystems());
		
		CoordinateAttributeDefinition coordAttrDef = (CoordinateAttributeDefinition) 
				survey.getSchema().getDefinitionById(coordinateAttributeId);

		RecordFilter filter = new RecordFilter(survey);
		filter.setOffset(recordOffset);
		filter.setMaxNumberOfRecords(maxNumberOfRecords);
		List<CollectRecord> summaries = recordManager.loadSummaries(filter);
		for (CollectRecord summary : summaries) {
			CollectRecord record = recordManager.load(survey, summary.getId(), summary.getStep(), false);
			List<Node<?>> nodes = record.findNodesByPath(coordAttrDef.getPath());
			for (Node<?> node : nodes) {
				CoordinateAttribute coordAttr = (CoordinateAttribute) node;
				if (coordAttr.isFilled()) {
					Coordinate coordinate = coordAttr.getValue();
					Coordinate projectedCoord = coordinateOperations.convertTo(coordinate, toSrsId);
					coordinateProcessor.process(record, coordAttr, projectedCoord);
				}
			}
		}
	}
	
	public static class CoordinateAttributePoint {
		
		private CoordinateAttribute attribute;
		private Coordinate coordinate;

		public CoordinateAttributePoint(CoordinateAttribute attribute, Coordinate coordinate) {
			this.attribute = attribute;
			this.coordinate = coordinate;
		}
		
		public int getRecId() {
			return attribute.getRecord().getId();
		}
		
		public Step getRecStep() {
			return ((CollectRecord) attribute.getRecord()).getStep();
		}
		
		public List<String> getRecKeys() {
			CollectRecord record = (CollectRecord) this.attribute.getRecord();
			return record.getRootEntityKeyValues();
		}
		
		public int getAttrId() {
			return attribute.getInternalId();
		}
		
		public int getAttrDefId() {
			return attribute.getDefinition().getId();
		}
		
		public Coordinate getOriginalCoordinate() {
			return attribute.getValue();
		}
		
		public Double getX() {
			return coordinate == null ? null : coordinate.getX();
		}
		
		public Double getY() {
			return coordinate == null ? null : coordinate.getY();
		}
		
		/**
		 * Returns the distance to the expected location
		 * @return
		 */
		public Double getDistance() {
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