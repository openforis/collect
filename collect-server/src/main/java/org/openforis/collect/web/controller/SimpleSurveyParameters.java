package org.openforis.collect.web.controller;

import java.io.Serializable;

import org.openforis.collect.metamodel.SurveyViewGenerator.SurveyView.Distribution;
import org.openforis.collect.metamodel.SurveyViewGenerator.SurveyView.Shape;

public class SimpleSurveyParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	String name;
	private String description;
	double boundaryLonMin;
	double boundaryLonMax;
	double boundaryLatMin;
	double boundaryLatMax; 
	int numPlots;
	Distribution plotDistribution;
	double plotResolution;
	private Shape plotShape;
	double plotWidth;
	int samplesPerPlot;
	double sampleResolution;
	Distribution sampleDistribution;
	private String sampleShape;
	double sampleWidth;
	Object[] sampleValues;
	String[] imagery;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public double getBoundaryLonMin() {
		return boundaryLonMin;
	}
	public void setBoundaryLonMin(double boundaryLonMin) {
		this.boundaryLonMin = boundaryLonMin;
	}
	public double getBoundaryLonMax() {
		return boundaryLonMax;
	}
	public void setBoundaryLonMax(double boundaryLonMax) {
		this.boundaryLonMax = boundaryLonMax;
	}
	public double getBoundaryLatMin() {
		return boundaryLatMin;
	}
	public void setBoundaryLatMin(double boundaryLatMin) {
		this.boundaryLatMin = boundaryLatMin;
	}
	public double getBoundaryLatMax() {
		return boundaryLatMax;
	}
	public void setBoundaryLatMax(double boundaryLatMax) {
		this.boundaryLatMax = boundaryLatMax;
	}
	public int getNumPlots() {
		return numPlots;
	}
	public void setNumPlots(int numPlots) {
		this.numPlots = numPlots;
	}
	public Distribution getPlotDistribution() {
		return plotDistribution;
	}
	public void setPlotDistribution(Distribution plotDistribution) {
		this.plotDistribution = plotDistribution;
	}
	public double getPlotResolution() {
		return plotResolution;
	}
	public void setPlotResolution(double plotResolution) {
		this.plotResolution = plotResolution;
	}
	public Shape getPlotShape() {
		return plotShape;
	}
	public void setPlotShape(Shape plotShape) {
		this.plotShape = plotShape;
	}
	public double getPlotWidth() {
		return plotWidth;
	}
	public void setPlotWidth(double plotWidth) {
		this.plotWidth = plotWidth;
	}
	public int getSamplesPerPlot() {
		return samplesPerPlot;
	}
	public void setSamplesPerPlot(int samplesPerPlot) {
		this.samplesPerPlot = samplesPerPlot;
	}
	public double getSampleResolution() {
		return sampleResolution;
	}
	public void setSampleResolution(double sampleResolution) {
		this.sampleResolution = sampleResolution;
	}
	public Distribution getSampleDistribution() {
		return sampleDistribution;
	}
	public void setSampleDistribution(Distribution sampleDistribution) {
		this.sampleDistribution = sampleDistribution;
	}
	public String getSampleShape() {
		return sampleShape;
	}
	public void setSampleShape(String sampleShape) {
		this.sampleShape = sampleShape;
	}
	public double getSampleWidth() {
		return sampleWidth;
	}
	public void setSampleWidth(double sampleWidth) {
		this.sampleWidth = sampleWidth;
	}
	public Object[] getSampleValues() {
		return sampleValues;
	}
	public void setSampleValues(Object[] sampleValues) {
		this.sampleValues = sampleValues;
	}
	public String[] getImagery() {
		return imagery;
	}
	public void setImagery(String[] imagery) {
		this.imagery = imagery;
	}
	
	
}