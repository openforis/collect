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
	private String bingMapsKey;
	private String planetMapsKey;
	private String extraMapUrl;
	private boolean openBingMaps;
	private boolean openPlanetMaps;
	private boolean openYandexMaps;
	private boolean openGEEExplorer;
	private boolean openGEECodeEditor;
	private boolean openGEEApp;
	private boolean openSecureWatch;
	private boolean openStreetView;
	
	private String defaultDescription;
	private String defaultProjectName;
	
	private boolean keyChangeAllowed;
	
	@Override
	public void loadFrom(CollectSurvey source, String languageCode) {
		name = source.getName();
		description = source.getDescription(languageCode);
		published = source.isPublished();
		projectName = source.getProjectName(languageCode);
		
		defaultProjectName = source.getProjectName();
		defaultDescription = source.getDescription();
		
		CollectAnnotations annotations = source.getAnnotations();
		collectEarthPlotArea = toListitemValue(annotations.getCollectEarthPlotArea());
		collectEarthSamplePoints = String.valueOf(annotations.getCollectEarthSamplePoints());
		bingMapsKey = annotations.getBingMapsKey();
		planetMapsKey = annotations.getPlanetMapsKey();
		extraMapUrl = annotations.getExtraMapUrl();
		openBingMaps = annotations.isBingMapsEnabled();
		openPlanetMaps = annotations.isPlanetMapsEnabled();
		openYandexMaps = annotations.isYandexMapsEnabled();
		openStreetView = annotations.isStreetViewEnabled();
		openGEEExplorer = annotations.isGEEExplorerEnabled();
		openGEECodeEditor = annotations.isGEECodeEditorEnabled();
		openGEEApp = annotations.isGEEAppEnabled();
		openSecureWatch = annotations.isSecureWatchEnabled();
		keyChangeAllowed = annotations.isKeyChangeAllowed();
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
		annotations.setBingMapsKey(bingMapsKey);
		annotations.setPlanetMapsKey(planetMapsKey);
		annotations.setExtraMapUrl(extraMapUrl);
		annotations.setBingMapsEnabled( openBingMaps );
		annotations.setPlanetMapsEnabled( openPlanetMaps );
		annotations.setYandexMapsEnabled( openYandexMaps );
		annotations.setStreetViewEnabled( openStreetView );
		annotations.setGEECodeEditorEnabled( openGEECodeEditor );
		annotations.setGEEAppEnabled( openGEEApp );
		annotations.setSecureWatchEnabled( openSecureWatch );
		annotations.setGEEExplorerEnabled(openGEEExplorer );
		annotations.setKeyChangeAllowed(keyChangeAllowed);
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
	
	public String getPlanetMapsKey() {
		return planetMapsKey;
	}

	public void setPlanetMapsKey(String planetMapsKey) {
		this.planetMapsKey = planetMapsKey;
	}

	public boolean isOpenPlanetMaps() {
		return openPlanetMaps;
	}

	public void setOpenPlanetMaps(boolean openPlanetMaps) {
		this.openPlanetMaps = openPlanetMaps;
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

	public String getBingMapsKey() {
		return bingMapsKey;
	}

	public void setBingMapsKey(String bingMapsKey) {
		this.bingMapsKey = bingMapsKey;
	}

	public String getExtraMapUrl() {
		return extraMapUrl;
	}
	
	public void setExtraMapUrl(String extraMapUrl) {
		this.extraMapUrl = extraMapUrl;
	}
	
	public boolean isOpenBingMaps() {
		return openBingMaps;
	}

	public void setOpenBingMaps(boolean openBingMaps) {
		this.openBingMaps = openBingMaps;
	}
	
	public boolean isOpenYandexMaps() {
		return openYandexMaps;
	}

	public void setOpenYandexMaps(boolean openYandexMaps) {
		this.openYandexMaps = openYandexMaps;
	}

	public boolean isOpenGEEExplorer() {
		return openGEEExplorer;
	}

	public void setOpenGEEExplorer(boolean openGEEExplorer) {
		this.openGEEExplorer = openGEEExplorer;
	}

	public boolean isOpenGEECodeEditor() {
		return openGEECodeEditor;
	}

	public void setOpenGEECodeEditor(boolean openGEECodeEditor) {
		this.openGEECodeEditor = openGEECodeEditor;
	}
	
	public boolean isOpenGEEApp() {
		return openGEEApp;
	}

	public void setOpenGEEApp(boolean openGEEApp) {
		this.openGEEApp = openGEEApp;
	}
	
	public boolean isOpenSecureWatch() {
		return openSecureWatch;
	}

	public void setOpenSecureWatch(boolean openSecureWatch) {
		this.openSecureWatch = openSecureWatch;
	}

	public boolean isOpenStreetView() {
		return openStreetView;
	}

	public void setOpenStreetView(boolean openStreetView) {
		this.openStreetView = openStreetView;
	}

	public String getDefaultProjectName() {
		return defaultProjectName;
	}
	
	public String getDefaultDescription() {
		return defaultDescription;
	}
	
	public boolean isKeyChangeAllowed() {
		return keyChangeAllowed;
	}
	
	public void setKeyChangeAllowed(boolean keyChangeAllowed) {
		this.keyChangeAllowed = keyChangeAllowed;
	}
}
