package org.openforis.collect.web.controller;

import static org.openforis.collect.utils.Controllers.KML_CONTENT_TYPE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.geospatial.GeoToolsCoordinateOperations;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NodeProcessor;
import org.openforis.collect.model.RecordCoordinatesKmlGeneratorJob;
import org.openforis.collect.model.RecordFilter;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.model.AbstractValue;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
import org.openforis.idm.model.TextAttribute;
import org.openforis.idm.model.TextValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
public class GeoDataController {

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private CollectJobManager jobManager;
	
	@RequestMapping(value = "survey/{surveyId}/data/coordinatevalues.json", method=GET)
	public @ResponseBody List<CoordinateAttributePoint> loadCoordinateValues(
			@PathVariable int surveyId, 
			@RequestParam int coordinateAttributeId,
			@RequestParam String srsId,
			@RequestParam int recordOffset, 
			@RequestParam int maxNumberOfRecords) throws Exception {
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

	@RequestMapping(value = "survey/{surveyId}/data/geometries.json", method=GET)
	public @ResponseBody List<GeometryNodeInfo> loadGeometryValues(
			@PathVariable int surveyId, 
			@RequestParam int attributeId,
			@RequestParam String srsId,
			@RequestParam int recordOffset, 
			@RequestParam int maxNumberOfRecords) throws Exception {
		final List<GeometryNodeInfo> result = new ArrayList<GeometryNodeInfo>();
		CollectSurvey survey = surveyManager.loadSurvey(surveyId);
		
		processNodes(survey, recordOffset, maxNumberOfRecords, 
				attributeId, new NodeProcessor() {
			public void process(Node<?> node) throws Exception {
				if (! node.isEmpty()) {
					result.add(new GeometryNodeInfo((TextAttribute) node));
				}
			}
		});
		return result;
	}

	@RequestMapping(value = "survey/{surveyId}/data/coordinatesvalues.kml", method=GET, produces=KML_CONTENT_TYPE)
	public void createCoordinateValuesKML(
			@PathVariable("surveyId") int surveyId, 
			@RequestParam int stepNum,
			@RequestParam int coordinateAttributeId,
			HttpServletResponse response) throws Exception {
		CollectSurvey survey = surveyManager.getById(surveyId);
		CoordinateAttributeDefinition nodeDef = (CoordinateAttributeDefinition) survey.getSchema().getDefinitionById(coordinateAttributeId);
		
		RecordCoordinatesKmlGeneratorJob job = new RecordCoordinatesKmlGeneratorJob();
		job.setRecordManager(recordManager);
		
		RecordFilter filter = new RecordFilter(survey);
		job.setRecordFilter(filter);
		job.setNodeDefinition(nodeDef);
		job.setOutput(response.getOutputStream());
		GeoToolsCoordinateOperations coordinateOperations = new GeoToolsCoordinateOperations();
		coordinateOperations.registerSRS(survey.getSpatialReferenceSystems());
		job.setCoordinateOperations(coordinateOperations);
		
		jobManager.start(job, false);
	}
	
	private void extractAllRecordCoordinates(CollectSurvey survey, Integer recordOffset, Integer maxNumberOfRecords, 
			int coordinateAttributeId, final String toSrsId, final CoordinateProcessor coordinateProcessor) throws Exception {
		final GeoToolsCoordinateOperations coordinateOperations = new GeoToolsCoordinateOperations();
		coordinateOperations.registerSRS(survey.getSpatialReferenceSystems());
		
		processNodes(survey, recordOffset, maxNumberOfRecords, coordinateAttributeId, new NodeProcessor() {
			public void process(Node<?> node) throws Exception {
				CoordinateAttribute coordAttr = (CoordinateAttribute) node;
				if (coordAttr.isFilled()) {
					Coordinate coordinate = coordAttr.getValue();
					Coordinate projectedCoord = coordinateOperations.convertTo(coordinate, toSrsId);
					coordinateProcessor.process((CollectRecord) node.getRecord(), coordAttr, projectedCoord);
				}
			}
		});
	}
	
	private void processNodes(CollectSurvey survey, Integer recordOffset, Integer maxNumberOfRecords, 
			int attributeId, NodeProcessor nodeProcessor) throws Exception {
		GeoToolsCoordinateOperations coordinateOperations = new GeoToolsCoordinateOperations();
		coordinateOperations.registerSRS(survey.getSpatialReferenceSystems());
		
		NodeDefinition nodeDef = survey.getSchema().getDefinitionById(attributeId);

		RecordFilter filter = new RecordFilter(survey);
		filter.setOffset(recordOffset);
		filter.setMaxNumberOfRecords(maxNumberOfRecords);
		
		List<CollectRecord> summaries = recordManager.loadSummaries(filter);
		for (CollectRecord summary : summaries) {
			CollectRecord record = recordManager.load(survey, summary.getId(), summary.getStep(), false);
			List<Node<?>> nodes = record.findNodesByPath(nodeDef.getPath());
			for (Node<?> node : nodes) {
				nodeProcessor.process(node);
			}
		}
	}
	
	public static abstract class NodeInfo<T extends Node<?>> {
		
		protected T node;
		
		public NodeInfo(T node) {
			super();
			this.node = node;
		}

		public int getRecId() {
			return node.getRecord().getId();
		}
		
		public Step getRecStep() {
			return ((CollectRecord) node.getRecord()).getStep();
		}
		
		public List<String> getRecKeys() {
			CollectRecord record = (CollectRecord) this.node.getRecord();
			return record.getRootEntityKeyValues();
		}
		
		public List<RecordDataItem> getRecordData() {
			final List<RecordDataItem> result = new ArrayList<RecordDataItem>();
			CollectSurvey survey = (CollectSurvey) this.node.getSurvey();
			final CollectAnnotations annotations = survey.getAnnotations();
			this.node.getRecord().getRootEntity().visitChildren(new NodeVisitor() {
				public void visit(Node<? extends NodeDefinition> node, int idx) {
					if (node instanceof Attribute && annotations.isShowInMapBalloon((AttributeDefinition) node.getDefinition())) {
						Attribute<?,?> attr = (Attribute<?, ?>) node;
						AbstractValue val = (AbstractValue) attr.getValue();
						String value = val.toPrettyFormatString();
						result.add(new RecordDataItem(node.getDefinition().getId(), value));
					}
				}
			}, true);
			return result;
		}
		
		public int getAttrId() {
			return node.getInternalId();
		}
		
		public int getAttrDefId() {
			return node.getDefinition().getId();
		}
	}
	
	public static class RecordDataItem {
		
		private int definitionId;
		private String value;
		
		public RecordDataItem(int definitionId, String value) {
			super();
			this.definitionId = definitionId;
			this.value = value;
		}

		public int getDefinitionId() {
			return definitionId;
		}

		public void setDefinitionId(int definitionId) {
			this.definitionId = definitionId;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
	
	public static class GeometryNodeInfo extends NodeInfo<TextAttribute> {

		public GeometryNodeInfo(TextAttribute node) {
			super(node);
		}
		
		public String getGeometry() {
			TextValue value = node.getValue();
			return value.getValue();
		}
	}
	
	public static class CoordinateAttributePoint extends NodeInfo<CoordinateAttribute> {
		
		private Coordinate coordinate;

		public CoordinateAttributePoint(CoordinateAttribute attribute, Coordinate coordinate) {
			super(attribute);
			this.coordinate = coordinate;
		}
		
		public Coordinate getOriginalCoordinate() {
			return ((CoordinateAttribute) node).getValue();
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
			CoordinateAttributeDefinition def = (CoordinateAttributeDefinition) this.node.getDefinition();
			DistanceCheck distanceCheck = def.extractMaxDistanceCheck();
			if (distanceCheck == null) {
				return null;
			} else {
				return distanceCheck.evaluateDistanceToDestination((CoordinateAttribute) node);
			}
		}
	}
	
	private interface CoordinateProcessor {
		
		void process(CollectRecord record, CoordinateAttribute coordAttr, Coordinate wgs84Coordinate);
	}
}