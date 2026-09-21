package org.openforis.collect.io.metadata.collectearth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;

/**
 * The external services that Collect Earth can open next to Google Earth while a plot is interpreted,
 * as they can be configured in its properties dialog.
 *
 * A survey can carry a Planet API key for everybody who opens the project file; when it does not, the key
 * that each interpreter has set in their own Collect Earth is left untouched.
 */
public class CollectEarthExternalServices {

	/** First mosaic published by the Planet Tropical Forest Observatory */
	private static final int FIRST_MONTHLY_MOSAIC_YEAR = 2020;
	private static final int FIRST_MONTHLY_MOSAIC_MONTH = 9;

	/** The mosaics published before the monthly ones covered six months each */
	private static final String[] BIANNUAL_MOSAICS = {
			"2015-12_2016-05", "2016-06_2016-11", "2016-12_2017-05", "2017-06_2017-11", "2017-12_2018-05",
			"2018-06_2018-11", "2018-12_2019-05", "2019-06_2019-11", "2019-12_2020-05", "2020-06_2020-08" };

	private boolean geeAppEnabled;
	private String geeAppDateFrom;
	private String geeAppDateTo;
	private boolean earthMapEnabled;
	private boolean esriWaybackEnabled;
	private boolean planetMapsEnabled;
	private String planetMapsKey;
	private boolean planetMapsUseTfo;
	private String planetTfoDateFrom;
	private String planetTfoDateTo;
	private boolean secureWatchEnabled;
	private String secureWatchUrl;
	private boolean streetViewEnabled;
	private String extraMapUrl;

	public static CollectEarthExternalServices fromSurvey(CollectSurvey survey) {
		CollectAnnotations annotations = survey.getAnnotations();
		CollectEarthExternalServices services = new CollectEarthExternalServices();
		services.geeAppEnabled = annotations.isGEEAppEnabled();
		services.geeAppDateFrom = annotations.getGEEAppDateFrom();
		services.geeAppDateTo = annotations.getGEEAppDateTo();
		services.earthMapEnabled = annotations.isEarthMapEnabled();
		services.esriWaybackEnabled = annotations.isEsriWaybackEnabled();
		services.planetMapsEnabled = annotations.isPlanetMapsEnabled();
		services.planetMapsKey = annotations.getPlanetMapsKey();
		services.planetMapsUseTfo = annotations.isPlanetMapsUseTfo();
		services.planetTfoDateFrom = annotations.getPlanetTfoDateFrom();
		services.planetTfoDateTo = annotations.getPlanetTfoDateTo();
		services.secureWatchEnabled = annotations.isSecureWatchEnabled();
		services.secureWatchUrl = annotations.getSecureWatchUrl();
		services.streetViewEnabled = annotations.isStreetViewEnabled();
		services.extraMapUrl = annotations.getExtraMapUrl();
		return services;
	}

	public void saveTo(CollectSurvey survey) {
		CollectAnnotations annotations = survey.getAnnotations();
		annotations.setGEEAppEnabled(geeAppEnabled);
		annotations.setGEEAppDateFrom(geeAppDateFrom);
		annotations.setGEEAppDateTo(geeAppDateTo);
		annotations.setEarthMapEnabled(earthMapEnabled);
		annotations.setEsriWaybackEnabled(esriWaybackEnabled);
		annotations.setPlanetMapsEnabled(planetMapsEnabled);
		annotations.setPlanetMapsKey(planetMapsKey);
		annotations.setPlanetMapsUseTfo(planetMapsUseTfo);
		annotations.setPlanetTfoDateFrom(planetTfoDateFrom);
		annotations.setPlanetTfoDateTo(planetTfoDateTo);
		annotations.setSecureWatchEnabled(secureWatchEnabled);
		annotations.setSecureWatchUrl(secureWatchUrl);
		annotations.setStreetViewEnabled(streetViewEnabled);
		annotations.setExtraMapUrl(extraMapUrl);
	}

	public static CollectEarthExternalServices fromProjectProperties(Properties p) {
		CollectEarthExternalServices services = new CollectEarthExternalServices();
		services.geeAppEnabled = getBooleanProperty(p, "open_gee_app");
		services.geeAppDateFrom = getStringProperty(p, "geeapp_date_from");
		services.geeAppDateTo = getStringProperty(p, "geeapp_date_to");
		services.earthMapEnabled = getBooleanProperty(p, "open_earth_map");
		services.esriWaybackEnabled = getBooleanProperty(p, "open_esri_wayback");
		services.planetMapsEnabled = getBooleanProperty(p, "open_planet_maps");
		services.planetMapsKey = getStringProperty(p, "planet_maps_key");
		services.planetMapsUseTfo = getBooleanProperty(p, "planet_maps_use_tfo");
		services.planetTfoDateFrom = getStringProperty(p, "planet_tfo_date_from");
		services.planetTfoDateTo = getStringProperty(p, "planet_tfo_date_to");
		services.secureWatchEnabled = getBooleanProperty(p, "open_maxar_securewatch");
		services.secureWatchUrl = getStringProperty(p, "secure_watch_url");
		services.streetViewEnabled = getBooleanProperty(p, "open_street_view");
		services.extraMapUrl = getStringProperty(p, "extra_map_url");
		return services;
	}

	/**
	 * Every service is written, also when it is switched off, because Collect Earth only overwrites the
	 * properties present in the project file: a missing one keeps the value of the project loaded before this one.
	 * The Planet API key and the URL of SecureWatch are the exceptions: they are only written when the survey
	 * has them, so that an empty one does not take away the key of the interpreter or the URL that Collect Earth
	 * ships with.
	 */
	public void writeProjectProperties(Properties p) {
		p.put("open_gee_app", String.valueOf(geeAppEnabled));
		// an empty date range means all the imagery that the GEE app has
		p.put("geeapp_date_from", geeAppEnabled ? StringUtils.trimToEmpty(geeAppDateFrom) : "");
		p.put("geeapp_date_to", geeAppEnabled ? StringUtils.trimToEmpty(geeAppDateTo) : "");
		p.put("open_earth_map", String.valueOf(earthMapEnabled));
		p.put("open_esri_wayback", String.valueOf(esriWaybackEnabled));
		p.put("open_planet_maps", String.valueOf(planetMapsEnabled));
		if (StringUtils.isNotBlank(planetMapsKey)) {
			p.put("planet_maps_key", planetMapsKey.trim());
		}
		p.put("planet_maps_use_tfo", String.valueOf(planetMapsEnabled && planetMapsUseTfo));
		// an empty mosaic means the first (or the latest) one available
		boolean tfoInUse = planetMapsEnabled && planetMapsUseTfo;
		p.put("planet_tfo_date_from", tfoInUse ? StringUtils.trimToEmpty(planetTfoDateFrom) : "");
		p.put("planet_tfo_date_to", tfoInUse ? StringUtils.trimToEmpty(planetTfoDateTo) : "");
		p.put("open_maxar_securewatch", String.valueOf(secureWatchEnabled));
		if (StringUtils.isNotBlank(secureWatchUrl)) {
			p.put("secure_watch_url", secureWatchUrl.trim());
		}
		p.put("open_street_view", String.valueOf(streetViewEnabled));
		p.put("extra_map_url", StringUtils.trimToEmpty(extraMapUrl));
	}

	/**
	 * Mosaics of the Planet Tropical Forest Observatory that can be chosen, oldest first:
	 * the biannual ones and then one per month up to the month before the current one.
	 */
	public static List<String> getPlanetTfoMosaics(Calendar today) {
		List<String> mosaics = new ArrayList<String>();
		for (String biannual : BIANNUAL_MOSAICS) {
			mosaics.add(biannual);
		}
		// the mosaic of the current month is not published yet
		Calendar lastMosaic = (Calendar) today.clone();
		lastMosaic.add(Calendar.MONTH, -1);
		Calendar mosaic = Calendar.getInstance();
		mosaic.clear();
		mosaic.set(FIRST_MONTHLY_MOSAIC_YEAR, FIRST_MONTHLY_MOSAIC_MONTH - 1, 1);
		while (!mosaic.after(lastMosaic)) {
			mosaics.add(String.format("%d-%02d", mosaic.get(Calendar.YEAR), mosaic.get(Calendar.MONTH) + 1));
			mosaic.add(Calendar.MONTH, 1);
		}
		return mosaics;
	}

	public static List<String> getPlanetTfoMosaics() {
		return getPlanetTfoMosaics(Calendar.getInstance());
	}

	private static boolean getBooleanProperty(Properties p, String key) {
		return Boolean.parseBoolean(StringUtils.trimToEmpty(p.getProperty(key)));
	}

	private static String getStringProperty(Properties p, String key) {
		return StringUtils.trimToNull(p.getProperty(key));
	}

	// ========== Getters and setters ==========

	public boolean isGEEAppEnabled() {
		return geeAppEnabled;
	}

	public void setGEEAppEnabled(boolean geeAppEnabled) {
		this.geeAppEnabled = geeAppEnabled;
	}

	public String getGEEAppDateFrom() {
		return geeAppDateFrom;
	}

	public void setGEEAppDateFrom(String geeAppDateFrom) {
		this.geeAppDateFrom = geeAppDateFrom;
	}

	public String getGEEAppDateTo() {
		return geeAppDateTo;
	}

	public void setGEEAppDateTo(String geeAppDateTo) {
		this.geeAppDateTo = geeAppDateTo;
	}

	public boolean isEarthMapEnabled() {
		return earthMapEnabled;
	}

	public void setEarthMapEnabled(boolean earthMapEnabled) {
		this.earthMapEnabled = earthMapEnabled;
	}

	public boolean isEsriWaybackEnabled() {
		return esriWaybackEnabled;
	}

	public void setEsriWaybackEnabled(boolean esriWaybackEnabled) {
		this.esriWaybackEnabled = esriWaybackEnabled;
	}

	public boolean isPlanetMapsEnabled() {
		return planetMapsEnabled;
	}

	public void setPlanetMapsEnabled(boolean planetMapsEnabled) {
		this.planetMapsEnabled = planetMapsEnabled;
	}

	public String getPlanetMapsKey() {
		return planetMapsKey;
	}

	public void setPlanetMapsKey(String planetMapsKey) {
		this.planetMapsKey = planetMapsKey;
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

	public boolean isSecureWatchEnabled() {
		return secureWatchEnabled;
	}

	public void setSecureWatchEnabled(boolean secureWatchEnabled) {
		this.secureWatchEnabled = secureWatchEnabled;
	}

	public String getSecureWatchUrl() {
		return secureWatchUrl;
	}

	public void setSecureWatchUrl(String secureWatchUrl) {
		this.secureWatchUrl = secureWatchUrl;
	}

	public boolean isStreetViewEnabled() {
		return streetViewEnabled;
	}

	public void setStreetViewEnabled(boolean streetViewEnabled) {
		this.streetViewEnabled = streetViewEnabled;
	}

	public String getExtraMapUrl() {
		return extraMapUrl;
	}

	public void setExtraMapUrl(String extraMapUrl) {
		this.extraMapUrl = extraMapUrl;
	}
}
