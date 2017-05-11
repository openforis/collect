package org.openforis.idm.model.expression.internal;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.openforis.idm.geospatial.CoordinateOperationException;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResult;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResultFlag;
import org.openforis.idm.model.Coordinate;

import com.jamesmurty.utils.XMLBuilder2;


public class GeoFunctions extends CustomFunctions {
	
	public static final String LATLONG_FUNCTION_NAME = "latlong";
	
	public GeoFunctions(String namespace) {
		super(namespace);
	
		register("polygon", new CustomFunction(1) {
			public Object invoke(ExpressionContext context, Object[] objects) {
				if (objects == null || objects.length == 0 || (objects.length == 1 && objects[0] == null)) {
					return null;
				}
				Collection<Coordinate> coordinates = toListOfCoordinates(objects[0]);
				ModelJXPathContext jxPathContext = (ModelJXPathContext) context.getJXPathContext();
				Survey survey = jxPathContext.getSurvey();
				
				try {
					String placemarkName = "Polygon name";
					CoordinateOperations coordinateOperations = survey.getContext().getCoordinateOperations();
					
					return writeKmlPolygon(coordinates, placemarkName, coordinateOperations);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			
			@Override
			protected ExpressionValidationResult performArgumentValidation(NodeDefinition contextNodeDef,
					Expression[] arguments) {
				return super.performArgumentValidation(contextNodeDef, arguments);
			}
		});
		
		register("distance", new CustomFunction(2) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return distance(expressionContext, objects[0], objects[1]);
			}
			@Override
			protected ExpressionValidationResult performArgumentValidation(NodeDefinition contextNodeDef,
					Expression[] arguments) {
				return super.performArgumentValidation(contextNodeDef, arguments);
			}
		});
		
		register(LATLONG_FUNCTION_NAME, new CustomFunction(1) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return latLong(expressionContext, objects[0]);
			}
			protected ExpressionValidationResult performArgumentValidation(NodeDefinition contextNodeDef,
					Expression[] arguments) {
				Survey survey = contextNodeDef.getSurvey();
				if(survey.getSpatialReferenceSystem(SpatialReferenceSystem.WGS84_SRS_ID) == null) {
					String message = String.format("%s function requires a lat long Spatial Reference System defined with id '%s'", 
							LATLONG_FUNCTION_NAME, SpatialReferenceSystem.WGS84_SRS_ID);
					return new ExpressionValidationResult(ExpressionValidationResultFlag.ERROR, message);
				} else {
					return new ExpressionValidationResult();
				}
			}
		});

	}

	private Collection<Coordinate> toListOfCoordinates(Object obj) {
		if (obj instanceof Collection) {
			@SuppressWarnings("unchecked")
			Collection<Object> list = (Collection<Object>) obj;
			Collection<Coordinate> coordinates = new ArrayList<Coordinate>(list.size());
			for (Object c: list) {
				if (c instanceof Coordinate) {
					coordinates.add((Coordinate) c);
				} else {
					Coordinate coord = Coordinate.parseCoordinate(c);
					if (coord != null) {
						coordinates.add(coord);
					}
				}
			}
			return coordinates;
		} else if (obj instanceof Coordinate) {
			return Arrays.asList((Coordinate) obj);
		} else {
			Coordinate coord = Coordinate.parseCoordinate(obj);
			if (coord == null) {
				return Collections.emptyList();
			} else {
				return Arrays.asList(coord);
			}
		}
	}
	
	private Object writeKmlPolygon(Collection<Coordinate> coordinates, String placemarkName,
			CoordinateOperations coordinateOperations) throws ParserConfigurationException,
			TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		
		String coordinatesText = coordinatesToString(coordinates, coordinateOperations);
		
		XMLBuilder2 builder = XMLBuilder2.create("kml", "http://www.opengis.net/kml/2.2")
				.e("Placemark")
					.e("name").text(placemarkName)
					.up()
					.e("Polygon")
						.e("outerBoundaryIs")
							.e("LinearRing")
								.e("coordinates").t(coordinatesText);
		
		//write XML to String
		StringWriter writer = new StringWriter();
		@SuppressWarnings("serial")
		Properties outputProperties = new Properties(){{
			put(javax.xml.transform.OutputKeys.INDENT, "yes");
			put(javax.xml.transform.OutputKeys.STANDALONE, "yes");
		}};
		builder.toWriter(writer, outputProperties);
		return writer.toString();
	}

	private String coordinatesToString(Collection<Coordinate> coordinates, CoordinateOperations coordinateOperations) {
		StringBuilder coordinatesSb = new StringBuilder();
		for (Coordinate coordinate : coordinates) {
			if (coordinate.isComplete()) {
				Coordinate latLonCoord = coordinateOperations.convertToWgs84(coordinate);
				coordinatesSb.append(latLonCoord.getX());
				coordinatesSb.append(',');
				coordinatesSb.append(latLonCoord.getY());
				coordinatesSb.append('\n');
			}
		}
		return coordinatesSb.toString();
	}
	
	/**
	 * Calculates the orthodromic distance between 2 coordinates (in meters)
	 */
	protected static Double distance(ExpressionContext context, Object from, Object to) {
		if (from == null || to == null) {
			return null;
		}
		Coordinate fromC = from instanceof Coordinate ? (Coordinate) from: Coordinate.parseCoordinate(from);
		if (fromC == null || ! fromC.isComplete()) {
			return null;
		}
		Coordinate toC = to instanceof Coordinate ? (Coordinate) to: Coordinate.parseCoordinate(to);
		if (toC == null || ! toC.isComplete()) {
			return null;
		}
		CoordinateOperations coordinateOperations = getSurvey(context).getContext().getCoordinateOperations();
		if (coordinateOperations == null) {
			return null;
		} else {
			try {
				double distance = coordinateOperations.orthodromicDistance(fromC, toC);
				return distance;
			} catch (CoordinateOperationException e) {
				return null;
			}
		}
	}

	protected static Coordinate latLong(ExpressionContext expressionContext, Object coordinate) {
		if (coordinate == null) {
			return null;
		}
		if (coordinate instanceof Coordinate) {
			return latLong(expressionContext, (Coordinate) coordinate);
		} else {
			return latLong(expressionContext, Coordinate.parseCoordinate(coordinate));
		}
	}

	protected static Coordinate latLong(ExpressionContext expressionContext, Coordinate coordinate) {
		if (coordinate == null || ! coordinate.isComplete()) {
			return null;
		}
		Survey survey = getSurvey(expressionContext);
		CoordinateOperations coordinateOperations = survey.getContext().getCoordinateOperations();
		if (coordinateOperations == null) {
			return null;
		} else {
			return coordinateOperations.convertToWgs84(coordinate);
		}
	}
}
