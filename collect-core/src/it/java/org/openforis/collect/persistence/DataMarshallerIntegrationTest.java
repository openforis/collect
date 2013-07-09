package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.persistence.xml.DataUnmarshallerException;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class DataMarshallerIntegrationTest extends CollectIntegrationTest {
	
	@Autowired
	private DataMarshaller dataMarshaller;
	
	@Autowired
	private RecordManager recordManager;
	
	private static Map<String, User> users;
	
	@BeforeClass
	public static void init() {
		users = new HashMap<String, User>();
		User user = new User();
		user.setId(1);
		user.setName("admin");
		users.put(user.getName(), user);
		user = new User();
		user.setId(2);
		user.setName("data_entry");
		users.put(user.getName(), user);
	}
	
	@Test
	public void testMarshal() throws Exception  {
		// LOAD MODEL
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		
		StringWriter out = new StringWriter();

		dataMarshaller.write(record, out);
		String xml = out.toString();
		assertNotNull(xml);
		
		ParseRecordResult parseRecordResult = parseRecord(survey, xml);
		
		assertNotNull(parseRecordResult);
		
		CollectRecord record2 = parseRecordResult.getRecord();

		assertNotNull(record2);
		
		assertEquals(record, record2);
	}
	
	private ParseRecordResult parseRecord(CollectSurvey survey, String xml) throws IOException, DataUnmarshallerException {
		DataHandler dataHandler = new DataHandler(survey, users);
		DataUnmarshaller dataUnmarshaller = new DataUnmarshaller(dataHandler);
		StringReader reader = new StringReader(xml);
		ParseRecordResult result = dataUnmarshaller.parse(reader);
		return result;
	}
	
	private CollectRecord createTestRecord(CollectSurvey survey) {
		CollectRecord record = new CollectRecord(survey, "2.0");
		User user = users.get("admin");
		record.setCreatedBy(user);
		record.setModifiedBy(user);
		Entity cluster = record.createRootEntity("cluster");
		record.setCreationDate(new GregorianCalendar(2011, 11, 31, 23, 59).getTime());
		record.setModifiedDate(new GregorianCalendar(2012, 2, 3, 9, 30).getTime());
		record.setStep(Step.ENTRY);
		record.setState(State.REJECTED);
		
		addTestValues(cluster);

		//update counts and keys
		record.updateRootEntityKeyValues();
		record.updateEntityCounts();
		
		return record;
	}
	
	private void addTestValues(Entity cluster) {
		EntityBuilder.addValue(cluster, "id", new Code("123_456"));
		EntityBuilder.addValue(cluster, "gps_realtime", Boolean.TRUE);
		EntityBuilder.addValue(cluster, "region", new Code("001"));
		CodeAttribute districtAttr = EntityBuilder.addValue(cluster, "district", new Code("XXX"));
		recordManager.confirmError(districtAttr);
		EntityBuilder.addValue(cluster, "crew_no", 10);
		EntityBuilder.addValue(cluster, "map_sheet", "value 1");
		EntityBuilder.addValue(cluster, "map_sheet", "value 2");
		EntityBuilder.addValue(cluster, "vehicle_location", new Coordinate((double)432423423l, (double)4324324l, "srs"));
		EntityBuilder.addValue(cluster, "gps_model", "TomTom 1.232");
		recordManager.approveMissingValue(cluster, "accessibility");
		EntityBuilder.addValue(cluster, "remarks", "Remarks with UTF-8 character: Í");
		{
			Entity ts = EntityBuilder.addEntity(cluster, "time_study");
			EntityBuilder.addValue(ts, "date", new Date(2011,2,14));
			EntityBuilder.addValue(ts, "start_time", new Time(8,15));
			EntityBuilder.addValue(ts, "end_time", new Time(15,29));
		}
		{
			Entity ts = EntityBuilder.addEntity(cluster, "time_study");
			EntityBuilder.addValue(ts, "date", new Date(2011,2,15));
			EntityBuilder.addValue(ts, "start_time", new Time(8,32));
			EntityBuilder.addValue(ts, "end_time", new Time(11,20));
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("1"));
			Entity tree1 = EntityBuilder.addEntity(plot, "tree");
			EntityBuilder.addValue(tree1, "tree_no", 1);
			EntityBuilder.addValue(tree1, "dbh", 54.2);
			EntityBuilder.addValue(tree1, "total_height", 2.0);
//			EntityBuilder.addValue(tree1, "bole_height", (Double) null).setMetadata(new CollectAttributeMetadata('*',null,"No value specified"));
			RealAttribute boleHeight = EntityBuilder.addValue(tree1, "bole_height", (Double) null);
			boleHeight.getField(0).setSymbol(FieldSymbol.BLANK_ON_FORM.getCode());
			boleHeight.getField(0).setRemarks("No value specified");
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
