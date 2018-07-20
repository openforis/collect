package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.validation.DistanceCheck;

import liquibase.util.StringUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class DistanceCheckFormObject extends CheckFormObject<DistanceCheck> {
	
	private String destinationPointExpression;
	private String minDistanceExpression;
	private String maxDistanceExpression;
	private String sourcePointExpression;
	
	@Override
	public void saveTo(DistanceCheck dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setDestinationPointExpression(StringUtils.trimToNull(destinationPointExpression));
		dest.setMaxDistanceExpression(StringUtils.trimToNull(maxDistanceExpression));
		dest.setMinDistanceExpression(StringUtils.trimToNull(minDistanceExpression));
		dest.setSourcePointExpression(StringUtils.trimToNull(sourcePointExpression));
	}
	
	@Override
	public void loadFrom(DistanceCheck source, String languageCode) {
		super.loadFrom(source, languageCode);
		destinationPointExpression = source.getDestinationPointExpression();
		maxDistanceExpression = source.getMaxDistanceExpression();
		minDistanceExpression = source.getMinDistanceExpression();
		sourcePointExpression = source.getSourcePointExpression();
	}

	public String getDestinationPointExpression() {
		return destinationPointExpression;
	}

	public void setDestinationPointExpression(String destinationPointExpression) {
		this.destinationPointExpression = destinationPointExpression;
	}

	public String getMinDistanceExpression() {
		return minDistanceExpression;
	}

	public void setMinDistanceExpression(String minDistanceExpression) {
		this.minDistanceExpression = minDistanceExpression;
	}

	public String getMaxDistanceExpression() {
		return maxDistanceExpression;
	}

	public void setMaxDistanceExpression(String maxDistanceExpression) {
		this.maxDistanceExpression = maxDistanceExpression;
	}

	public String getSourcePointExpression() {
		return sourcePointExpression;
	}

	public void setSourcePointExpression(String sourcePointExpression) {
		this.sourcePointExpression = sourcePointExpression;
	}

}
