package org.openforis.collect.model;

import java.io.OutputStream;
import java.util.List;

import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Node;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RecordCoordinatesKmlGeneratorJob extends RecordIteratorJob {

	//input
	private CoordinateAttributeDefinition nodeDefinition;
	private OutputStream output;
	//optional
	private CoordinateOperations coordinateOperations;
	
	//temporary
	private Document kmlDoc;
	private Kml kml;
	
	@Override
	protected void initializeInternalVariables() throws Throwable {
		super.initializeInternalVariables();
		CollectSurvey survey = nodeDefinition.getSurvey();
		kml = new Kml();
		kmlDoc = kml.createAndSetDocument().withName(survey.getName());
		if (coordinateOperations == null) {
			coordinateOperations = nodeDefinition.getSurvey().getContext().getCoordinateOperations();
		}
	}
	
	@Override
	protected void processRecord(CollectRecord record) {
		List<Node<?>> nodes = record.findNodesByPath(nodeDefinition.getPath());
		for (Node<?> node : nodes) {
			processAttribute((CoordinateAttribute) node);
		}
	}

	private void processAttribute(CoordinateAttribute coordAttr) {
		if (coordAttr.isFilled()) {
			Coordinate coordinate = coordAttr.getValue();
			Coordinate wgs84Coordinate = coordinateOperations.convertTo(coordinate, SpatialReferenceSystem.WGS84_SRS_ID);
			kmlDoc.createAndAddPlacemark()
				.withName(((CollectRecord) coordAttr.getRecord()).getRootEntityKeyValues().toString())
				.withOpen(Boolean.TRUE)
				.createAndSetPoint()
				.addToCoordinates(wgs84Coordinate.getY(), wgs84Coordinate.getX());
		}
	}
	
	@Override
	protected void onCompleted() {
		super.onCompleted();
		try {
			kml.marshal(output);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setNodeDefinition(CoordinateAttributeDefinition nodeDefinition) {
		this.nodeDefinition = nodeDefinition;
	}
	
	public void setOutput(OutputStream output) {
		this.output = output;
	}
	
	public void setCoordinateOperations(CoordinateOperations coordinateOperations) {
		this.coordinateOperations = coordinateOperations;
	}

}
