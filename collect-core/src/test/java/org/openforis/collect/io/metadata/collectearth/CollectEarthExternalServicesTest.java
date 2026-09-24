package org.openforis.collect.io.metadata.collectearth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_EXTRA_MAP_URL;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_GEE_APP_DATE_FROM;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_GEE_APP_DATE_TO;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_OPEN_EARTH_MAP;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_OPEN_ESRI_WAYBACK;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_OPEN_GEE_APP;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_OPEN_MAXAR_SECUREWATCH;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_OPEN_PLANET_MAPS;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_OPEN_STREET_VIEW;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_PLANET_MAPS_KEY;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_PLANET_MAPS_USE_TFO;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_PLANET_TFO_DATE_FROM;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_PLANET_TFO_DATE_TO;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices.PROPERTY_KEY_SECURE_WATCH_URL;

import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;

/**
 * The external services of a survey have to reach Collect Earth as the properties its own options dialog writes.
 */
public class CollectEarthExternalServicesTest {

	private CollectSurvey survey;
	private CollectAnnotations annotations;

	@Before
	public void init() {
		survey = (CollectSurvey) new CollectSurveyContext().createSurvey();
		annotations = survey.getAnnotations();
	}

	@Test
	public void everyServiceIsWrittenAlsoWhenItIsSwitchedOff() {
		// Collect Earth keeps the value of the project loaded before this one for the properties that are not present
		Properties p = writeProjectProperties();
		assertEquals("true", p.get(PROPERTY_KEY_OPEN_GEE_APP)); // on by default
		assertEquals("false", p.get(PROPERTY_KEY_OPEN_EARTH_MAP));
		assertEquals("false", p.get(PROPERTY_KEY_OPEN_ESRI_WAYBACK));
		assertEquals("false", p.get(PROPERTY_KEY_OPEN_MAXAR_SECUREWATCH));
		assertEquals("false", p.get(PROPERTY_KEY_OPEN_STREET_VIEW));
		assertEquals("", p.get(PROPERTY_KEY_EXTRA_MAP_URL));
	}

	@Test
	public void geeAppDatesAreWrittenAsCollectEarthReadsThem() {
		annotations.setGEEAppEnabled(true);
		annotations.setGEEAppDateFrom("2015-06-01");
		annotations.setGEEAppDateTo("2020-12-31");

		Properties p = writeProjectProperties();
		assertEquals("2015-06-01", p.get(PROPERTY_KEY_GEE_APP_DATE_FROM));
		assertEquals("2020-12-31", p.get(PROPERTY_KEY_GEE_APP_DATE_TO));
	}

	@Test
	public void geeAppDatesAreClearedWhenTheAppIsNotOpened() {
		annotations.setGEEAppEnabled(false);
		annotations.setGEEAppDateFrom("2015-06-01");
		annotations.setGEEAppDateTo("2020-12-31");

		Properties p = writeProjectProperties();
		assertEquals("false", p.get(PROPERTY_KEY_OPEN_GEE_APP));
		assertEquals("", p.get(PROPERTY_KEY_GEE_APP_DATE_FROM));
		assertEquals("", p.get(PROPERTY_KEY_GEE_APP_DATE_TO));
	}

	@Test
	public void planetMosaicsAreOnlyWrittenWhenTheObservatoryIsUsed() {
		annotations.setPlanetMapsEnabled(true);
		annotations.setPlanetTfoDateFrom("2021-03");
		annotations.setPlanetTfoDateTo("2021-09");

		Properties p = writeProjectProperties();
		assertEquals("true", p.get(PROPERTY_KEY_OPEN_PLANET_MAPS));
		assertEquals("false", p.get(PROPERTY_KEY_PLANET_MAPS_USE_TFO));
		assertEquals("", p.get(PROPERTY_KEY_PLANET_TFO_DATE_FROM));
		assertEquals("", p.get(PROPERTY_KEY_PLANET_TFO_DATE_TO));

		annotations.setPlanetMapsUseTfo(true);
		p = writeProjectProperties();
		assertEquals("true", p.get(PROPERTY_KEY_PLANET_MAPS_USE_TFO));
		assertEquals("2021-03", p.get(PROPERTY_KEY_PLANET_TFO_DATE_FROM));
		assertEquals("2021-09", p.get(PROPERTY_KEY_PLANET_TFO_DATE_TO));
	}

	@Test
	public void theApiKeyOfPlanetIsOnlyWrittenWhenTheSurveyHasOne() {
		annotations.setPlanetMapsEnabled(true);
		annotations.setPlanetMapsUseTfo(true);

		// without one, the key that the interpreter set in their own Collect Earth is left alone
		assertNull(writeProjectProperties().get(PROPERTY_KEY_PLANET_MAPS_KEY));

		annotations.setPlanetMapsKey("PLAK1234567890");
		assertEquals("PLAK1234567890", writeProjectProperties().get(PROPERTY_KEY_PLANET_MAPS_KEY));
	}

	@Test
	public void theSecureWatchUrlIsOnlyWrittenWhenTheSurveyHasOne() {
		// an empty value would leave Collect Earth without the URL it ships with
		assertNull(writeProjectProperties().get(PROPERTY_KEY_SECURE_WATCH_URL));

		annotations.setSecureWatchEnabled(true);
		annotations.setSecureWatchUrl("https://securewatch.maxar.com/?config=my_own");
		assertEquals("https://securewatch.maxar.com/?config=my_own", writeProjectProperties().get(PROPERTY_KEY_SECURE_WATCH_URL));
	}

	@Test
	public void servicesSurviveTheRoundTripThroughAProjectFile() {
		annotations.setGEEAppEnabled(true);
		annotations.setGEEAppDateFrom("2015-06-01");
		annotations.setGEEAppDateTo("2020-12-31");
		annotations.setEarthMapEnabled(true);
		annotations.setEsriWaybackEnabled(true);
		annotations.setPlanetMapsEnabled(true);
		annotations.setPlanetMapsKey("PLAK1234567890");
		annotations.setPlanetMapsUseTfo(true);
		annotations.setPlanetTfoDateFrom("2021-03");
		annotations.setPlanetTfoDateTo("");
		annotations.setSecureWatchEnabled(true);
		annotations.setSecureWatchUrl("https://securewatch.maxar.com/?config=my_own");
		annotations.setStreetViewEnabled(true);
		annotations.setExtraMapUrl("https://www.extramap.org/lat=LATITUDE&long=LONGITUDE");

		CollectEarthExternalServices restored = CollectEarthExternalServices
				.fromProjectProperties(writeProjectProperties());

		assertTrue(restored.isGEEAppEnabled());
		assertEquals("2015-06-01", restored.getGEEAppDateFrom());
		assertEquals("2020-12-31", restored.getGEEAppDateTo());
		assertTrue(restored.isEarthMapEnabled());
		assertTrue(restored.isEsriWaybackEnabled());
		assertTrue(restored.isPlanetMapsEnabled());
		assertEquals("PLAK1234567890", restored.getPlanetMapsKey());
		assertTrue(restored.isPlanetMapsUseTfo());
		assertEquals("2021-03", restored.getPlanetTfoDateFrom());
		assertNull("the latest mosaic available is stored as an empty value", restored.getPlanetTfoDateTo());
		assertTrue(restored.isSecureWatchEnabled());
		assertEquals("https://securewatch.maxar.com/?config=my_own", restored.getSecureWatchUrl());
		assertTrue(restored.isStreetViewEnabled());
		assertEquals("https://www.extramap.org/lat=LATITUDE&long=LONGITUDE", restored.getExtraMapUrl());
	}

	@Test
	public void restoredServicesAreStoredInTheSurveyAnnotations() {
		Properties p = new Properties();
		p.put(PROPERTY_KEY_OPEN_GEE_APP, "false");
		p.put(PROPERTY_KEY_OPEN_ESRI_WAYBACK, "true");
		p.put(PROPERTY_KEY_OPEN_PLANET_MAPS, "true");
		p.put(PROPERTY_KEY_PLANET_MAPS_USE_TFO, "true");
		p.put(PROPERTY_KEY_PLANET_TFO_DATE_FROM, "2022-01");
		p.put(PROPERTY_KEY_SECURE_WATCH_URL, "https://securewatch.maxar.com/?config=other");

		CollectEarthExternalServices.fromProjectProperties(p).saveTo(survey);

		assertFalse(annotations.isGEEAppEnabled());
		assertTrue(annotations.isEsriWaybackEnabled());
		assertTrue(annotations.isPlanetMapsEnabled());
		assertTrue(annotations.isPlanetMapsUseTfo());
		assertEquals("2022-01", annotations.getPlanetTfoDateFrom());
		assertEquals("https://securewatch.maxar.com/?config=other", annotations.getSecureWatchUrl());
	}

	@Test
	public void planetMosaicsRunFromTheBiannualOnesToTheMonthBeforeToday() {
		Calendar march2022 = Calendar.getInstance();
		march2022.clear();
		march2022.set(2022, Calendar.MARCH, 15);

		List<String> mosaics = CollectEarthExternalServices.getPlanetTfoMosaics(march2022);

		assertEquals("2015-12_2016-05", mosaics.get(0));
		assertEquals("2020-06_2020-08", mosaics.get(9));
		assertEquals("2020-09", mosaics.get(10));
		// the mosaic of the current month is not published yet
		assertEquals("2022-02", mosaics.get(mosaics.size() - 1));
		assertFalse(mosaics.contains("2022-03"));
	}

	private Properties writeProjectProperties() {
		Properties p = new Properties();
		CollectEarthExternalServices.fromSurvey(survey).writeProjectProperties(p);
		return p;
	}
}
