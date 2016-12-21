package org.openforis.collect.metamodel.samplingdesign;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.model.Coordinate;

public class SamplingPointGenerationSettings {
	
	private List<Coordinate> aoiBoundary = new ArrayList<Coordinate>();
	private List<SamplingPointLevelGenerationSettings> levelsSettings = 
			new ArrayList<SamplingPointLevelGenerationSettings>(3);
	
	public List<Coordinate> getAoiBoundary() {
		return aoiBoundary;
	}
	
	public void setAoiBoundary(List<Coordinate> aoiBoundary) {
		this.aoiBoundary = aoiBoundary;
	}
	
	public List<SamplingPointLevelGenerationSettings> getLevelsSettings() {
		return levelsSettings;
	}
	
	public void setLevelsSettings(List<SamplingPointLevelGenerationSettings> levelsSettings) {
		this.levelsSettings = levelsSettings;
	}
	
}