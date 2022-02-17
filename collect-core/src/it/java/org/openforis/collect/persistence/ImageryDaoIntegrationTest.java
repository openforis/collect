package org.openforis.collect.persistence;

import org.junit.Test;
import org.openforis.collect.CollectTest;
import org.openforis.collect.model.Imagery;
import org.springframework.beans.factory.annotation.Autowired;

import org.junit.Assert;

public class ImageryDaoIntegrationTest extends CollectTest {

	@Autowired
	private ImageryDao dao;
	
	@Test
	public void testInsert() {
		Imagery imagery = new Imagery();
		imagery.setTitle("DigitalGlobeWMSImagery");
		imagery.setAttribution("DigitalGlobe BaseMap WMS Imagery | © DigitalGlobe, Inc");
		imagery.setExtent(null);
		imagery.setSourceConfig("{type: \"GeoServer\", "
                 + "geoserverUrl: \"https://services.digitalglobe.com/mapservice/wmsaccess\","
                 + "geoserverParams: {\"VERSION\": \"1.1.1\","
                                   +"\"LAYERS\": \"DigitalGlobe:Imagery\","
                                   + "\"CONNECTID]\": \"63f634af-fc31-4d81-9505-b62b4701f8a9\"}}");
		dao.insert(imagery);
		
		Assert.assertNotNull(imagery.getId());
	}
}
