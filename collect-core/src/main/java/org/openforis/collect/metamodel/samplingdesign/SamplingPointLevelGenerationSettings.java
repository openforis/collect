package org.openforis.collect.metamodel.samplingdesign;

public class SamplingPointLevelGenerationSettings {
	
	public enum Shape {
		CIRCLE, SQUARE
	}
	
	public enum Distribution {
		RANDOM, GRIDDED, CSV
	}
	
	private int numPoints;
	private Shape shape;
	private Distribution distribution;
	private double resolution;
	private double pointWidth;
	
	public SamplingPointLevelGenerationSettings() {
		super();
	}
	
	public SamplingPointLevelGenerationSettings(int numPoints, Shape shape, Distribution distribution, double resolution, double pointWidth) {
		this.numPoints = numPoints;
		this.shape = shape;
		this.distribution = distribution;
		this.resolution = resolution;
		this.pointWidth = pointWidth;
	}
	
	public int getNumPoints() {
		return numPoints;
	}

	public void setNumPoints(int numPoints) {
		this.numPoints = numPoints;
	}

	public Shape getShape() {
		return shape;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}

	public Distribution getDistribution() {
		return distribution;
	}

	public void setDistribution(Distribution distribution) {
		this.distribution = distribution;
	}

	public double getResolution() {
		return resolution;
	}

	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	public double getPointWidth() {
		return pointWidth;
	}

	public void setPointWidth(double pointWidth) {
		this.pointWidth = pointWidth;
	}
}