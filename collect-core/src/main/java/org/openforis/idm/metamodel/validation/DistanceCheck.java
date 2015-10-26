/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	private static final Log LOG = LogFactory.getLog(DistanceCheck.class);

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
	
	public Coordinate evaluateDestinationPoint(CoordinateAttribute attr) {
		if (destinationPointExpression == null) {
			return null;
		}
		ExpressionEvaluator expressionEvaluator = getExpressionEvaluator(attr);
		try {
			Coordinate destinationPoint = expressionEvaluator.evaluateAttributeValue(attr.getParent(), attr, attr.getDefinition(), destinationPointExpression);
			return destinationPoint;
		} catch (InvalidExpressionException e) {
			if( LOG.isWarnEnabled() ){
				LOG.warn(String.format("[survey %s: coordinate attribute: %s] Unable to evaluate destination point using expression %s" + 
						attr.getSurvey().getName(), attr.getPath(), destinationPointExpression), e);
			}
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

}