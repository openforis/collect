/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.path.Path;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author K. Waga
 * @author S. Ricci
 */
public class DistanceCheck extends Check<CoordinateAttribute> {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LogManager.getLogger(DistanceCheck.class);

	private String destinationPointExpression;
	private String minDistanceExpression;
	private String maxDistanceExpression;
	private String sourcePointExpression;

	@Override
	public String buildExpression() {
		StringBuilder sb = new StringBuilder();
		if (maxDistanceExpression != null) {
			sb.append(createDistanceExpression());
			sb.append(" < ");
			sb.append(maxDistanceExpression);
		}
		if (minDistanceExpression != null) {
			if (sb.length() > 0) {
				sb.append(" and ");
			}
			sb.append(createDistanceExpression());
			sb.append(" > ");
			sb.append(minDistanceExpression);
		}
		return sb.toString();
	}
	
	@Override
	public ValidationResultFlag evaluate(CoordinateAttribute coordinateAttr) {
		CoordinateOperations coordinateOperations = getCoordinateOperations(coordinateAttr);
		if ( coordinateOperations == null ) {
			return ValidationResultFlag.OK;
		}
		try {
			ExpressionEvaluator expressionEvaluator = getExpressionEvaluator(coordinateAttr);
			boolean valid = expressionEvaluator.evaluateBoolean(coordinateAttr.getParent(), coordinateAttr, getExpression());
			return ValidationResultFlag.valueOf(valid, this.getFlag());
		} catch (InvalidExpressionException e) {
			if( LOG.isInfoEnabled() ){
				LOG.info("Unable to evaluate distance check " , e);
			}
			return ValidationResultFlag.OK;
		}
	}
	
	public Coordinate evaluateDestinationPoint(CoordinateAttribute thisNode) {
		return evaluateAttributeValueExpression(destinationPointExpression, thisNode);
	}
	
	public Double evaluateMinDistance(CoordinateAttribute thisNode) {
		return evaluateNumericExpression(minDistanceExpression, thisNode);
	}
	
	public Double evaluateMaxDistance(CoordinateAttribute thisNode) {
		return evaluateNumericExpression(maxDistanceExpression, thisNode);
	}
	
	public Double evaluateDistanceToDestination(CoordinateAttribute thisNode) {
		String distanceExpression = createDistanceExpression();
		Double result = evaluateNumericExpression(distanceExpression, thisNode);
		return result;
	}
	
	private Double evaluateNumericExpression(String expression, CoordinateAttribute thisNode) {
		Object result = evaluateValueExpression(expression, thisNode);
		if (result == null) {
			return null;
		} else if (result instanceof Double) {
			return (Double) result;
		} else if (result instanceof Number) {
			return Double.valueOf(((Number) result).doubleValue());
		} else {
			return Double.valueOf(result.toString());
		}
	}
	
	private <T extends Object> T evaluateAttributeValueExpression(String expression, CoordinateAttribute thisNode) {
		if (expression == null) {
			return null;
		}
		try {
			ExpressionEvaluator expressionEvaluator = getExpressionEvaluator(thisNode);
			T result = expressionEvaluator.evaluateAttributeValue(thisNode.getParent(), thisNode, thisNode.getDefinition(), expression);
			return result;
		} catch (InvalidExpressionException e) {
			LOG.warn(String.format("[survey %s: coordinate attribute: %s] Unable to evaluate expression %s" + 
					thisNode.getSurvey().getName(), thisNode.getPath(), expression), e);
			return null;
		}
	}
	
	private <T extends Object> T evaluateValueExpression(String expression, CoordinateAttribute thisNode) {
		if (expression == null) {
			return null;
		}
		try {
			ExpressionEvaluator expressionEvaluator = getExpressionEvaluator(thisNode);
			@SuppressWarnings("unchecked")
			T result = (T) expressionEvaluator.evaluateValue(thisNode.getParent(), thisNode, expression);
			return result;
		} catch (InvalidExpressionException e) {
			LOG.warn(String.format("[survey %s: coordinate attribute: %s] Unable to evaluate expression %s" + 
					thisNode.getSurvey().getName(), thisNode.getPath(), expression), e);
			return null;
		}
	}
	
	private String createDistanceExpression() {
		StringBuilder sb = new StringBuilder();
		sb.append(ExpressionFactory.IDM_PREFIX);
		sb.append(':');
		sb.append("distance");
		sb.append('(');
		if (sourcePointExpression == null) {
			sb.append(Path.THIS_VARIABLE);
		} else {
			sb.append(sourcePointExpression);
		}
		sb.append(',');
		sb.append(destinationPointExpression);
		sb.append(')');
		return sb.toString();
	}

	private ExpressionEvaluator getExpressionEvaluator(Node<?> node) {
		return node.getSurveyContext().getExpressionEvaluator();
	}
	
	private CoordinateOperations getCoordinateOperations(Node<?> node) {
		return node.getSurveyContext().getCoordinateOperations();
	}

	public String getDestinationPointExpression() {
		return destinationPointExpression;
	}

	public void setDestinationPointExpression(String destinationPointExpression) {
		this.destinationPointExpression = destinationPointExpression;
		resetExpression();
	}
	
	public String getMinDistanceExpression() {
		return minDistanceExpression;
	}

	public void setMinDistanceExpression(String minDistanceExpression) {
		this.minDistanceExpression = minDistanceExpression;
		resetExpression();
	}
	
	public String getMaxDistanceExpression() {
		return maxDistanceExpression;
	}

	public void setMaxDistanceExpression(String maxDistanceExpression) {
		this.maxDistanceExpression = maxDistanceExpression;
		resetExpression();
	}

	public String getSourcePointExpression() {
		return sourcePointExpression;
	}

	public void setSourcePointExpression(String sourcePointExpression) {
		this.sourcePointExpression = sourcePointExpression;
		resetExpression();
	}

	@Override
	public String toString() {
		return "DISTANCE - " + super.toString();
	}

}