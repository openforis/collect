package org.openforis.collect.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelSerializer;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.expression.ExpressionFactory;

public class ModelSerializationTest {
//	private final Log log = LogFactory.getLog(ModelSerializationTest.class);
	
	@Test
	public void testProto() throws Exception {
		// Create
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		Entity entity = record.getRootEntity();

		// Save
		ModelSerializer ms = new ModelSerializer(3000);
		byte[] data = ms.toByteArray(entity);
			
		// Load
		CollectRecord record2 = new CollectRecord(survey, "2.0");
		Entity reloadedEntity = record2.createRootEntity("cluster");
		ms.mergeFrom(data, reloadedEntity);
		
		Assert.assertEquals(entity, reloadedEntity);
	}
	
	private CollectSurvey loadSurvey() throws IOException, SurveyImportException, InvalidIdmlException {
		URL idm = ClassLoader.getSystemResource("test.idm.xml");
		InputStream is = idm.openStream();
		CollectSurveyContext context = new CollectSurveyContext(new ExpressionFactory(), null, null);
		CollectIdmlBindingContext idmlBindingContext = new CollectIdmlBindingContext(context);
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		survey.setName("archenland1");
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		int nextId = 0;
		for (EntityDefinition entityDefinition : rootEntityDefinitions) {
			nextId = initIds(entityDefinition, nextId);
		}
		return survey;
	}

	private int initIds(NodeDefinition nodeDefinition, int nextId) {
		nodeDefinition.setId(nextId++);
		if ( nodeDefinition instanceof EntityDefinition ) {
			EntityDefinition entityDefinition = (EntityDefinition) nodeDefinition;
			List<NodeDefinition> childDefinitions = entityDefinition.getChildDefinitions();
			for (NodeDefinition childDefn : childDefinitions) {
				nextId = initIds(childDefn, nextId);
			}
		}
		return nextId;
		
	}

	private CollectRecord createTestRecord(CollectSurvey survey) {
		CollectRecord record = new CollectRecord(survey, "2.0");
		Entity cluster = record.createRootEntity("cluster");
		record.setCreationDate(new GregorianCalendar(2011, 12, 31, 23, 59).getTime());
		record.setStep(Step.ENTRY);
		String id = "123_456";
		
		addTestValues(cluster, id);
			
		//set counts
		record.getEntityCounts().add(2);
		
		//set keys
		record.getRootEntityKeyValues().add(id);
		
		return record;
	}

	private void addTestValues(Entity cluster, String id) {
		cluster.addValue("id", new Code(id));
		cluster.addValue("gps_realtime", Boolean.TRUE);
		cluster.addValue("region", new Code("001"));
		cluster.addValue("district", new Code("002"));
		cluster.addValue("crew_no", 10);
		cluster.addValue("map_sheet", "value 1");
		cluster.addValue("map_sheet", "value 2");
		cluster.addValue("vehicle_location", new Coordinate((double)432423423l, (double)4324324l, "srs"));
		cluster.addValue("gps_model", "TomTom 1.232");
		{
			Entity ts = cluster.addEntity("time_study");
			ts.addValue("date", new Date(2011,2,14));
			ts.addValue("start_time", new Time(8,15));
			ts.addValue("end_time", new Time(15,29));
		}
		{
			Entity ts = cluster.addEntity("time_study");
			ts.addValue("date", new Date(2011,2,15));
			ts.addValue("start_time", new Time(8,32));
			ts.addValue("end_time", new Time(11,20));
		}
		{
			Entity plot = cluster.addEntity("plot");
			plot.addValue("no", new Code("1"));
			Entity tree1 = plot.addEntity("tree");
			tree1.addValue("tree_no", 1);
			tree1.addValue("dbh", 54.2);
			tree1.addValue("total_height", 2.0);
//			tree1.addValue("bole_height", (Double) null).setMetadata(new CollectAttributeMetadata('*',null,"No value specified"));
			RealAttribute boleHeight = tree1.addValue("bole_height", (Double) null);
			boleHeight.getField(0).setSymbol('*');
			boleHeight.getField(0).setRemarks("No value specified");
			Entity tree2 = plot.addEntity("tree");
			tree2.addValue("tree_no", 2);
			tree2.addValue("dbh", 82.8);
			tree2.addValue("total_height", 3.0);
		}
		{
			Entity plot = cluster.addEntity("plot");
			plot.addValue("no", new Code("2"));
			Entity tree1 = plot.addEntity("tree");
			tree1.addValue("tree_no", 1);
			tree1.addValue("dbh", 34.2);
			tree1.addValue("total_height", 2.0);
			Entity tree2 = plot.addEntity("tree");
			tree2.addValue("tree_no", 2);
			tree2.addValue("dbh", 85.8);
			tree2.addValue("total_height", 4.0);
		}
	}
}
