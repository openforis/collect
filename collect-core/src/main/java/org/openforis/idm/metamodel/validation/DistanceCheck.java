/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author K. Waga
 * @author S. Ricci
 */
public class DistanceCheck extends Check<CoordinateAttribute> {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(DistanceCheck.class);

	private String destinationPointExpression;
	private String minDistanceExpression;
	private String maxDistanceExpression;
	private String sourcePointExpression;

	public String getDestinationPointExpression() {
		return destinationPointExpression;
	}

	public String getMinDistanceExpression() {
		return minDistanceExpression;
	}

	public String getMaxDistanceExpression() {
		return maxDistanceExpression;
	}

	public String getSourcePointExpression() {
		return sourcePointExpression;
	}

	public void setDestinationPointExpression(String destinationPointExpression) {
		this.destinationPointExpression = destinationPointExpression;
	}

	public void setMinDistanceExpression(String minDistanceExpression) {
		this.minDistanceExpression = minDistanceExpression;
	}

	public void setMaxDistanceExpression(String maxDistanceExpression) {
		this.maxDistanceExpression = maxDistanceExpression;
	}

	public void setSourcePointExpression(String sourcePointExpression) {
		this.sourcePointExpression = sourcePointExpression;
	}

	@Override
	public ValidationResultFlag evaluate(CoordinateAttribute coordinateAttr) {
		CoordinateOperations coordinateOperations = getCoordinateOperations(coordinateAttr);
		if ( coordinateOperations == null ) {
			return ValidationResultFlag.OK;
		}
		try {
			boolean valid = true;

			Entity parentEntity = coordinateAttr.getParent();
			Coordinate from = evaluateCoordinate(getSourcePointExpression(), parentEntity, coordinateAttr, coordinateAttr.getValue());
			Coordinate to = evaluateCoordinate(getDestinationPointExpression(), parentEntity, coordinateAttr, null);

			if ( !(from == null || to == null) ) {
				double distance = coordinateOperations.orthodromicDistance(from, to);

				if (maxDistanceExpression != null) {
					double maxDistance = evaluateDistance(parentEntity, coordinateAttr, maxDistanceExpression);
					if (distance > maxDistance) {
						valid = false;
					}
				}
				if ( valid && minDistanceExpression != null) {
					double minDistance = evaluateDistance(parentEntity, coordinateAttr, minDistanceExpression);
					if (distance < minDistance) {
						valid = false;
					}
				}
			}

			return ValidationResultFlag.valueOf(valid, this.getFlag());
		} catch (Exception e) {
//			throw new IdmInterpretationError("Unable to execute distance check", e);
			if( LOG.isInfoEnabled() ){
				LOG.info("Unable to evaluate distance check " , e);
			}
			return ValidationResultFlag.OK;
		}
	}

	private double evaluateDistance(Entity context, Attribute<?, ?> thisNode, String expression) throws InvalidExpressionException {
		ExpressionEvaluator expressionEvaluator = getExpressionEvaluator(context);
		Double value = (Double) expressionEvaluator.evaluateValue(context, thisNode, expression);
		return value;
	}

	private Coordinate evaluateCoordinate(String expression, Node<?> context, Attribute<?, ?> thisNode, Coordinate defaultCoordinate) throws InvalidExpressionException {
		if (expression == null) {
			return defaultCoordinate;
		} else {
			ExpressionEvaluator expressionEvaluator = getExpressionEvaluator(context);
			Coordinate coordinate = (Coordinate) expressionEvaluator.evaluateValue(context, thisNode, expression);
			return coordinate;
		}
	}

	public ExpressionEvaluator getExpressionEvaluator(Node<?> context) {
		return context.getSurvey().getContext().getExpressionEvaluator();
	}
	
	public CoordinateOperations getCoordinateOperations(Node<?> context) {
		return context.getSurvey().getContext().getCoordinateOperations();
	}

}