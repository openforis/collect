package org.openforis.collect.designer.form;

import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.util.StringUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyMainInfoFormObject extends FormObject<CollectSurvey> {

	private String name;
	private boolean published;
	private String description;
	private String projectName;
	private String collectEarthSamplePoints;
	private String collectEarthPlotArea;
	
	@Override
	public void loadFrom(CollectSurvey source, String languageCode) {
		name = source.getName();
		description = source.getDescription(languageCode);
		published = source.isPublished();
		projectName = source.getProjectName(languageCode);
		CollectAnnotations annotations = source.getAnnotations();
		collectEarthPlotArea = toListitemValue(annotations.getCollectEarthPlotArea());
		collectEarthSamplePoints = annotations.getCollectEarthSamplePoints().toString();
	}

	protected String toListitemValue(Double number) {
		return StringUtils.trimTrailingCharacter(
				StringUtils.trimTrailingCharacter(number.toString().replace('.', '_'), '0'), '_');
	}
	
	@Override
	public void saveTo(CollectSurvey dest, String languageCode) {
		dest.setName(name);
		dest.setDescription(languageCode, description);
		dest.setProjectName(languageCode, projectName);
		dest.setPublished(published);
		CollectAnnotations annotations = dest.getAnnotations();
		annotations.setCollectEarthPlotArea(fromListitemValueToDouble(collectEarthPlotArea));
		annotations.setCollectEarthSamplePoints(Integer.parseInt(collectEarthSamplePoints));
	}

	protected double fromListitemValueToDouble(String value) {
		return Double.parseDouble(value.replace('_', '.'));
	}
	
	@Override
	protected void reset() {
		// TODO Auto-generated method stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isPublished() {
		return published;
	}
	
	public void setPublished(boolean published) {
		this.published = published;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public String getCollectEarthSamplePoints() {
		return collectEarthSamplePoints;
	}
	
	public void setCollectEarthSamplePoints(String collectEarthSamplePoints) {
		this.collectEarthSamplePoints = collectEarthSamplePoints;
	}
	
	public String getCollectEarthPlotArea() {
		return collectEarthPlotArea;
	}
	
	public void setCollectEarthPlotArea(String collectEarthPlotArea) {
		this.collectEarthPlotArea = collectEarthPlotArea;
	}

}
