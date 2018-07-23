package org.openforis.idm.model;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;
import org.openforis.idm.metamodel.DefaultSurveyContext;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;

import junit.framework.Assert;

/**
 * 
 * @author G. Miceli
 * 
 */
public class ProtostuffSerializationTest  {

	@Test
	public void testRoundTrip() throws Exception {
		// Set up
		Survey survey = getTestSurvey();
		//assignFakeNodeDefinitionIds(survey.getSchema());
		Record record1 = createTestRecord(survey);
		Entity cluster1 = record1.getRootEntity();
		
		// Write
		ModelSerializer ser = new ModelSerializer(10000);
		byte[] data = ser.toByteArray(cluster1);
		
		// Read
		Record record2 = new Record(survey, "2.0", "cluster");
		ser.mergeFrom(data, record2.getRootEntity());

		// Compare
		Assert.assertTrue(record1.getRootEntity().deepEquals(record2.getRootEntity()));
	}

	@Test
	public void testSkipRemovedEntity() throws Exception {
		// Set up
		Survey survey = getTestSurvey();
		//assignFakeNodeDefinitionIds(survey.getSchema());
		Record record1 = createTestRecord(survey);
		Entity cluster1 = record1.getRootEntity();
		
		// Write
		ModelSerializer ser = new ModelSerializer(10000);
		byte[] data = ser.toByteArray(cluster1);
		
		//remove data
		cluster1.remove("map_sheet", 1);
		cluster1.remove("map_sheet", 0);
		
		Schema schema = survey.getSchema();
		EntityDefinition clusterDefn = schema.getRootEntityDefinition("cluster");
		NodeDefinition mapSheetDefn = clusterDefn.getChildDefinition("map_sheet");
		clusterDefn.removeChildDefinition(mapSheetDefn);
		
		Record record2 = new Record(survey, "2.0", "cluster");
		ser.mergeFrom(data, record2.getRootEntity());
		
		// Compare
		Assert.assertTrue(record1.getRootEntity().deepEquals(record2.getRootEntity()));
	}
	
	@Test
	public void testSkipRemovedAttribute() throws Exception {
		// Set up
		Survey survey = getTestSurvey();
		//assignFakeNodeDefinitionIds(survey.getSchema());
		Record record1 = createTestRecord(survey);
		Entity cluster1 = record1.getRootEntity();
		
		// Write
		ModelSerializer ser = new ModelSerializer(10000);
		byte[] data = ser.toByteArray(cluster1);
		
		//remove attribute from record before comparing it with the new one
		cluster1.remove("crew_no", 0);

		//remove node definition from schema
		Schema schema = survey.getSchema();
		EntityDefinition clusterDefn = schema.getRootEntityDefinition("cluster");
		NodeDefinition crewNumDefn = clusterDefn.getChildDefinition("crew_no");
		clusterDefn.removeChildDefinition(crewNumDefn);
		
		Record record2 = new Record(survey, "2.0", "cluster");
		ser.mergeFrom(data, record2.getRootEntity());
		
		// Compare
		Assert.assertTrue(record1.getRootEntity().deepEquals(record2.getRootEntity()));
	}
	private Survey getTestSurvey() throws IOException, IdmlParseException {
		URL idm = ClassLoader.getSystemResource("test.idm.xml");
		InputStream is = idm.openStream();
		SurveyContext surveyContext = new DefaultSurveyContext();
		SurveyIdmlBinder parser = new SurveyIdmlBinder(surveyContext);
		return parser.unmarshal(is);
	}
	
	private Record createTestRecord(Survey survey) {
		Record record = new Record(survey, "2.0", "cluster");
		addTestValues(record.getRootEntity(), "123_456");
		return record;
	}

	private void addTestValues(Entity cluster, String id) {
		EntityBuilder.addValue(cluster, "id", new Code(id));
		EntityBuilder.addValue(cluster, "gps_realtime", Boolean.TRUE);
		CodeAttribute region = EntityBuilder.addValue(cluster, "region", new Code("001", "aqualiferxxxxxxxxxxxx"));
		region.getCodeField().getState().set(0,true);
		region.updateSummaryInfo();
		EntityBuilder.addValue(cluster, "district", new Code("002"));
		EntityBuilder.addValue(cluster, "crew_no", 10);
		EntityBuilder.addValue(cluster, "map_sheet", "value 1");
		EntityBuilder.addValue(cluster, "map_sheet", "value 2");
		EntityBuilder.addValue(cluster, "vehicle_location", new Coordinate((double) 12345, (double) 67890, "srs"));
		TextAttribute gpsModel = EntityBuilder.addValue(cluster, "gps_model", "TomTom 1.232");
		gpsModel.getTextField().getState().set(0,true);
		gpsModel.updateSummaryInfo();
		cluster.setChildState("accessibility", 1);
		
		{
			Entity ts = EntityBuilder.addEntity(cluster, "time_study");
			EntityBuilder.addValue(ts, "date", new Date(2011, 2, 14));
			EntityBuilder.addValue(ts, "start_time", new Time(8, 15));
			EntityBuilder.addValue(ts, "end_time", new Time(15, 29));
		}
		{
			Entity ts = EntityBuilder.addEntity(cluster, "time_study");
			EntityBuilder.addValue(ts, "date", new Date(2011, 2, 15));
			EntityBuilder.addValue(ts, "start_time", new Time(8, 32));
			EntityBuilder.addValue(ts, "end_time", new Time(11, 20));
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("1"));
			Entity tree1 = EntityBuilder.addEntity(plot, "tree");
			EntityBuilder.addValue(tree1, "tree_no", 1);
			EntityBuilder.addValue(tree1, "dbh", 54.2);
			EntityBuilder.addValue(tree1, "total_height", 2.0);
			// EntityBuilder.addValue(tree1, "bole_height", (Double) null).setMetadata(new
			// CollectAttributeMetadata('*',null,"No value specified"));
			RealAttribute boleHeight = EntityBuilder.addValue(tree1, "bole_height",
					(Double) null);
			boleHeight.getField(0).setSymbol('B');
			boleHeight.getField(0).setRemarks("No value specified");
			boleHeight.updateSummaryInfo();
			Entity tree2 = EntityBuilder.addEntity(plot, "tree");
			EntityBuilder.addValue(tree2, "tree_no", 2);
			EntityBuilder.addValue(tree2, "dbh", 82.8);
			EntityBuilder.addValue(tree2, "total_height", 3.0);
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("2"));
			Entity tree1 = EntityBuilder.addEntity(plot, "tree");
			EntityBuilder.addValue(tree1, "tree_no", 1);
			EntityBuilder.addValue(tree1, "dbh", 34.2);
			EntityBuilder.addValue(tree1, "total_height", 2.0);
			Entity tree2 = EntityBuilder.addEntity(plot, "tree");
			EntityBuilder.addValue(tree2, "tree_no", 2);
			EntityBuilder.addValue(tree2, "dbh", 85.8);
			EntityBuilder.addValue(tree2, "total_height", 4.0);
		}
	}
}
