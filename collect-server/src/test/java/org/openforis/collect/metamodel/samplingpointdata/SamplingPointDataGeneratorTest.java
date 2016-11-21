package org.openforis.collect.metamodel.samplingpointdata;

import java.util.List;

import org.junit.Test;
import org.openforis.collect.io.metadata.samplingpointdata.SamplingPointDataGenerator;
import org.openforis.collect.metamodel.SurveyViewGenerator.SurveyView.Distribution;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.model.Coordinate;

import junit.framework.Assert;

public class SamplingPointDataGeneratorTest {

	@Test
	public void gridGenerationTest() {
		SamplingPointDataGenerator generator = new SamplingPointDataGenerator();
		Coordinate upperLeftCoordinate = new Coordinate(12.369192d, 41.987927d, SpatialReferenceSystem.WGS84_SRS_ID); 
		Coordinate bottomRightCoordinate = new Coordinate(12.621191d, 41.802904d, SpatialReferenceSystem.WGS84_SRS_ID); 
		List<SamplingDesignItem> items = generator.generate(upperLeftCoordinate.getX(), bottomRightCoordinate.getX(), upperLeftCoordinate.getY(), bottomRightCoordinate.getY(), 
				16, Distribution.GRIDDED, 50, 200, 
				10, Distribution.GRIDDED, 1, 10);
		Assert.assertEquals(16 + 16*10, items.size());
	}
	
}
