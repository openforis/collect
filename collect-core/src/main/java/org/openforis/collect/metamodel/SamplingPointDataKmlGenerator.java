package org.openforis.collect.metamodel;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.LngLat;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.commons.lang.Strings;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.model.Coordinate;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;

public class SamplingPointDataKmlGenerator {
	
	private SamplingDesignManager samplingDesignManager;
	private CollectSurvey survey;
	
	private Kml kml;
	
	public SamplingPointDataKmlGenerator(SamplingDesignManager samplingDesignManager, CollectSurvey survey) {
		super();
		this.samplingDesignManager = samplingDesignManager;
		this.survey = survey;
	}

	public void generate() {
		Kml kml = KmlFactory.createKml();
		Document doc = kml.createAndSetDocument();
		List<SamplingDesignItem> samplingDesignItems = loadSamplingDesignItems();
		for (SamplingDesignItem item : samplingDesignItems) {
			Coordinate coordinate = new Coordinate(item.getX(), item.getY(), item.getSrsId());
			LngLat lngLatAlt = createLngLat(coordinate);
			doc.createAndAddPlacemark()
					.withName(Strings.joinNotBlank(item.getLevelCodes(), "|"))
					.withOpen(true)
					.createAndSetPoint()
					.addToCoordinates(lngLatAlt.getLongitude(), lngLatAlt.getLatitude());
		}
		this.kml = kml;
	}
	
	public void write(OutputStream output) {
		if (kml == null) {
			throw new IllegalStateException("KML not generated yet: call 'generate' first");
		}
		try {
			kml.marshal(output);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(String.format("Fail to generate KML for survey %s Sampling Point Data: %s", 
					survey.getName(), e.getMessage()), e);
		}
	}
	
	private List<SamplingDesignItem> loadSamplingDesignItems() {
		SamplingDesignSummaries samplingDesignSummaries = samplingDesignManager.loadBySurvey(survey.getId());
		List<SamplingDesignItem> samplingDesignItems = samplingDesignSummaries.getRecords();
		List<SamplingDesignItem> result = new ArrayList<SamplingDesignItem>();
		for (SamplingDesignItem item : samplingDesignItems) {
			result.add(item);
		}
		return result;
	}
	
	private LngLat createLngLat(Coordinate coord) {
		try {
			CollectSurveyContext surveyContext = survey.getContext();
			CoordinateOperations coordOpts = surveyContext.getCoordinateOperations();
			Coordinate wgs84Coord = coordOpts.convertToWgs84(coord);
			return new LngLat(wgs84Coord.getX(), wgs84Coord.getY());
		} catch(Exception e) {
			return null;
		}
	}

}
