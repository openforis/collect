package org.openforis.collect.designer.form;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices;
import org.openforis.collect.io.metadata.collectearth.CollectEarthPlotLayout;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.CollectAnnotations.CollectEarthPlotShape;
import org.openforis.collect.metamodel.CollectAnnotations.CollectEarthReferenceAreaShape;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectSurvey;

/**
 *
 * @author S. Ricci
 *
 */
public class SurveyMainInfoFormObject extends FormObject<CollectSurvey> {

	private static final String COLLECT_EARTH_DATE_FORMAT = "yyyy-MM-dd";

	private String name;
	private boolean published;
	private String description;
	private String projectName;
	private String collectEarthPlotShape;
	private String collectEarthSamplePoints;
	private Integer collectEarthDistanceBetweenSamplePoints;
	private Integer collectEarthDistanceToPlotBoundaries;
	private Integer collectEarthInnerPointSide;
	private Integer collectEarthLargeCentralPlotSide;
	private Integer collectEarthDistanceBetweenPlots;
	private String collectEarthReferenceAreaShape;
	private Integer collectEarthReferenceAreaDistance;
	private String planetMapsKey;
	private String extraMapUrl;
	private boolean openEarthMap;
	private boolean openPlanetMaps;
	private boolean openGEEApp;
	private Date geeAppDateFrom;
	private Date geeAppDateTo;
	private boolean openEsriWayback;
	private boolean planetMapsUseTfo;
	private String planetTfoDateFrom;
	private String planetTfoDateTo;
	private boolean openSecureWatch;
	private String secureWatchUrl;
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
		loadCollectEarthPlotLayout(CollectEarthPlotLayout.fromSurvey(source));
		planetMapsKey = annotations.getPlanetMapsKey();
		extraMapUrl = annotations.getExtraMapUrl();
		openEarthMap = annotations.isEarthMapEnabled();
		openPlanetMaps = annotations.isPlanetMapsEnabled();
		openStreetView = annotations.isStreetViewEnabled();
		openGEEApp = annotations.isGEEAppEnabled();
		geeAppDateFrom = parseDate(annotations.getGEEAppDateFrom());
		geeAppDateTo = parseDate(annotations.getGEEAppDateTo());
		openEsriWayback = annotations.isEsriWaybackEnabled();
		planetMapsUseTfo = annotations.isPlanetMapsUseTfo();
		// the mosaic that Collect Earth picks on its own is stored as an empty value, and it is an entry of the list
		planetTfoDateFrom = emptyIfNull(annotations.getPlanetTfoDateFrom());
		planetTfoDateTo = emptyIfNull(annotations.getPlanetTfoDateTo());
		openSecureWatch = annotations.isSecureWatchEnabled();
		secureWatchUrl = annotations.getSecureWatchUrl();
		keyChangeAllowed = annotations.isKeyChangeAllowed();
	}

	private void loadCollectEarthPlotLayout(CollectEarthPlotLayout layout) {
		collectEarthPlotShape = layout.getPlotShape().name();
		collectEarthSamplePoints = String.valueOf(layout.getSamplePoints());
		collectEarthDistanceBetweenSamplePoints = layout.getDistanceBetweenSamplePoints();
		collectEarthDistanceToPlotBoundaries = layout.getDistanceToPlotBoundaries();
		collectEarthInnerPointSide = layout.getInnerPointSide();
		collectEarthLargeCentralPlotSide = layout.getLargeCentralPlotSide();
		collectEarthDistanceBetweenPlots = layout.getDistanceBetweenPlots();
		collectEarthReferenceAreaShape = layout.getReferenceAreaShape().name();
		collectEarthReferenceAreaDistance = layout.getReferenceAreaDistance();
	}

	/**
	 * External services as they are being edited
	 */
	public CollectEarthExternalServices toCollectEarthExternalServices() {
		CollectEarthExternalServices services = new CollectEarthExternalServices();
		services.setGEEAppEnabled(openGEEApp);
		services.setGEEAppDateFrom(formatDate(geeAppDateFrom));
		services.setGEEAppDateTo(formatDate(geeAppDateTo));
		services.setEarthMapEnabled(openEarthMap);
		services.setEsriWaybackEnabled(openEsriWayback);
		services.setPlanetMapsEnabled(openPlanetMaps);
		services.setPlanetMapsKey(planetMapsKey);
		services.setPlanetMapsUseTfo(planetMapsUseTfo);
		services.setPlanetTfoDateFrom(planetTfoDateFrom);
		services.setPlanetTfoDateTo(planetTfoDateTo);
		services.setSecureWatchEnabled(openSecureWatch);
		services.setSecureWatchUrl(secureWatchUrl);
		services.setStreetViewEnabled(openStreetView);
		services.setExtraMapUrl(extraMapUrl);
		return services;
	}

	/**
	 * Plot layout as it is being edited; the numbers that have been left empty count as 0
	 */
	public CollectEarthPlotLayout toCollectEarthPlotLayout() {
		return createCollectEarthPlotLayout(collectEarthPlotShape, collectEarthSamplePoints,
				collectEarthDistanceBetweenSamplePoints, collectEarthDistanceToPlotBoundaries, collectEarthInnerPointSide,
				collectEarthLargeCentralPlotSide, collectEarthDistanceBetweenPlots, collectEarthReferenceAreaShape,
				collectEarthReferenceAreaDistance);
	}

	public static CollectEarthPlotLayout createCollectEarthPlotLayout(String plotShape, String samplePoints,
			Integer distanceBetweenSamplePoints, Integer distanceToPlotBoundaries, Integer innerPointSide,
			Integer largeCentralPlotSide, Integer distanceBetweenPlots, String referenceAreaShape,
			Integer referenceAreaDistance) {
		CollectEarthPlotLayout layout = new CollectEarthPlotLayout();
		layout.setPlotShape(plotShape == null ? null : CollectEarthPlotShape.valueOf(plotShape));
		layout.setSamplePoints(samplePoints == null ? 0 : Integer.parseInt(samplePoints));
		layout.setDistanceBetweenSamplePoints(zeroIfNull(distanceBetweenSamplePoints));
		layout.setDistanceToPlotBoundaries(zeroIfNull(distanceToPlotBoundaries));
		layout.setInnerPointSide(zeroIfNull(innerPointSide));
		layout.setLargeCentralPlotSide(zeroIfNull(largeCentralPlotSide));
		layout.setDistanceBetweenPlots(zeroIfNull(distanceBetweenPlots));
		layout.setReferenceAreaShape(referenceAreaShape == null ? null : CollectEarthReferenceAreaShape.valueOf(referenceAreaShape));
		layout.setReferenceAreaDistance(referenceAreaDistance);
		return layout;
	}

	private static int zeroIfNull(Integer value) {
		return value == null ? 0 : value;
	}

	@Override
	public void saveTo(CollectSurvey dest, String languageCode) {
		dest.setName(name);
		dest.setDescription(languageCode, description);
		dest.setProjectName(languageCode, projectName);
		dest.setPublished(published);
		CollectAnnotations annotations = dest.getAnnotations();
		if (dest.getTarget() == SurveyTarget.COLLECT_EARTH) {
			// the options that the selected plot shape does not use keep their values, in case the shape is changed again
			toCollectEarthPlotLayout().saveTo(dest);
		}
		annotations.setPlanetMapsKey(nullIfEmpty(planetMapsKey));
		annotations.setExtraMapUrl(extraMapUrl);
		annotations.setEarthMapEnabled( openEarthMap );
		annotations.setPlanetMapsEnabled( openPlanetMaps );
		annotations.setStreetViewEnabled( openStreetView );
		annotations.setGEEAppEnabled( openGEEApp );
		annotations.setGEEAppDateFrom(formatDate(geeAppDateFrom));
		annotations.setGEEAppDateTo(formatDate(geeAppDateTo));
		annotations.setEsriWaybackEnabled(openEsriWayback);
		annotations.setPlanetMapsUseTfo(planetMapsUseTfo);
		annotations.setPlanetTfoDateFrom(nullIfEmpty(planetTfoDateFrom));
		annotations.setPlanetTfoDateTo(nullIfEmpty(planetTfoDateTo));
		annotations.setSecureWatchEnabled( openSecureWatch );
		annotations.setSecureWatchUrl(secureWatchUrl);
		annotations.setKeyChangeAllowed(keyChangeAllowed);
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

	public String getCollectEarthPlotShape() {
		return collectEarthPlotShape;
	}

	public void setCollectEarthPlotShape(String collectEarthPlotShape) {
		this.collectEarthPlotShape = collectEarthPlotShape;
	}

	public Integer getCollectEarthDistanceBetweenSamplePoints() {
		return collectEarthDistanceBetweenSamplePoints;
	}

	public void setCollectEarthDistanceBetweenSamplePoints(Integer collectEarthDistanceBetweenSamplePoints) {
		this.collectEarthDistanceBetweenSamplePoints = collectEarthDistanceBetweenSamplePoints;
	}

	public Integer getCollectEarthDistanceToPlotBoundaries() {
		return collectEarthDistanceToPlotBoundaries;
	}

	public void setCollectEarthDistanceToPlotBoundaries(Integer collectEarthDistanceToPlotBoundaries) {
		this.collectEarthDistanceToPlotBoundaries = collectEarthDistanceToPlotBoundaries;
	}

	public Integer getCollectEarthInnerPointSide() {
		return collectEarthInnerPointSide;
	}

	public void setCollectEarthInnerPointSide(Integer collectEarthInnerPointSide) {
		this.collectEarthInnerPointSide = collectEarthInnerPointSide;
	}

	public Integer getCollectEarthLargeCentralPlotSide() {
		return collectEarthLargeCentralPlotSide;
	}

	public void setCollectEarthLargeCentralPlotSide(Integer collectEarthLargeCentralPlotSide) {
		this.collectEarthLargeCentralPlotSide = collectEarthLargeCentralPlotSide;
	}

	public Integer getCollectEarthDistanceBetweenPlots() {
		return collectEarthDistanceBetweenPlots;
	}

	public void setCollectEarthDistanceBetweenPlots(Integer collectEarthDistanceBetweenPlots) {
		this.collectEarthDistanceBetweenPlots = collectEarthDistanceBetweenPlots;
	}

	public String getCollectEarthReferenceAreaShape() {
		return collectEarthReferenceAreaShape;
	}

	public void setCollectEarthReferenceAreaShape(String collectEarthReferenceAreaShape) {
		this.collectEarthReferenceAreaShape = collectEarthReferenceAreaShape;
	}

	public Integer getCollectEarthReferenceAreaDistance() {
		return collectEarthReferenceAreaDistance;
	}

	public void setCollectEarthReferenceAreaDistance(Integer collectEarthReferenceAreaDistance) {
		this.collectEarthReferenceAreaDistance = collectEarthReferenceAreaDistance;
	}

	private static String emptyIfNull(String value) {
		return value == null ? "" : value;
	}

	private static String nullIfEmpty(String value) {
		return value == null || value.trim().isEmpty() ? null : value;
	}

	/**
	 * Collect Earth keeps the dates of the GEE app as yyyy-MM-dd text
	 */
	private static Date parseDate(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return new SimpleDateFormat(COLLECT_EARTH_DATE_FORMAT).parse(value);
		} catch (ParseException e) {
			return null;
		}
	}

	private static String formatDate(Date value) {
		return value == null ? null : new SimpleDateFormat(COLLECT_EARTH_DATE_FORMAT).format(value);
	}

	public Date getGeeAppDateFrom() {
		return geeAppDateFrom;
	}

	public void setGeeAppDateFrom(Date geeAppDateFrom) {
		this.geeAppDateFrom = geeAppDateFrom;
	}

	public Date getGeeAppDateTo() {
		return geeAppDateTo;
	}

	public void setGeeAppDateTo(Date geeAppDateTo) {
		this.geeAppDateTo = geeAppDateTo;
	}

	public boolean isOpenEsriWayback() {
		return openEsriWayback;
	}

	public void setOpenEsriWayback(boolean openEsriWayback) {
		this.openEsriWayback = openEsriWayback;
	}

	public boolean isPlanetMapsUseTfo() {
		return planetMapsUseTfo;
	}

	public void setPlanetMapsUseTfo(boolean planetMapsUseTfo) {
		this.planetMapsUseTfo = planetMapsUseTfo;
	}

	public String getPlanetTfoDateFrom() {
		return planetTfoDateFrom;
	}

	public void setPlanetTfoDateFrom(String planetTfoDateFrom) {
		this.planetTfoDateFrom = planetTfoDateFrom;
	}

	public String getPlanetTfoDateTo() {
		return planetTfoDateTo;
	}

	public void setPlanetTfoDateTo(String planetTfoDateTo) {
		this.planetTfoDateTo = planetTfoDateTo;
	}

	public String getSecureWatchUrl() {
		return secureWatchUrl;
	}

	public void setSecureWatchUrl(String secureWatchUrl) {
		this.secureWatchUrl = secureWatchUrl;
	}

	public String getExtraMapUrl() {
		return extraMapUrl;
	}

	public void setExtraMapUrl(String extraMapUrl) {
		this.extraMapUrl = extraMapUrl;
	}

	public boolean isOpenEarthMap() {
		return openEarthMap;
	}

	public void setOpenEarthMap(boolean openEarthMap) {
		this.openEarthMap = openEarthMap;
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
