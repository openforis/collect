package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.openforis.idm.testfixture.NodeBuilder.attribute;
import static org.openforis.idm.testfixture.NodeBuilder.entity;
import static org.openforis.idm.testfixture.RecordBuilder.record;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.LocalUserManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.manager.UserPersistenceException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.persistence.xml.DataUnmarshallerException;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.Time;
import org.openforis.idm.testfixture.RecordBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataMarshallerIntegrationTest extends CollectIntegrationTest {
	
	@Autowired
	private DataMarshaller dataMarshaller;
	@Autowired
	private LocalUserManager userManager;

	@Before
	public void init() throws UserPersistenceException {
		userManager.insertUser("entry", "pass1", UserRole.ENTRY);
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
		DataUnmarshaller dataUnmarshaller = new DataUnmarshaller(survey);
		StringReader reader = new StringReader(xml);
		ParseRecordResult result = dataUnmarshaller.parse(reader);
		return result;
	}
	
	private CollectRecord createTestRecord(CollectSurvey survey) {
		RecordBuilder recordBuilder = record(
			attribute("id", "123_456"),
			attribute("gps_realtime", "true"),
			attribute("region", "001"),
			attribute("district", "XXX"),
			attribute("crew_no", 10),
			attribute("map_sheet", "value 1"),
			attribute("map_sheet", "value 2"),
			attribute("vehicle_location", new Coordinate(432423423d, 4324324d, "srs")),
			attribute("gps_model", "TomTom 1.232"),
			attribute("remarks", "Remarks with UTF-8 character: Í"),
			entity("time_study",
				attribute("date", new Date(2011,2,14)),
				attribute("start_time", new Time(8,15)),
				attribute("end_time", new Time(15,29))
			),
			entity("time_study",
				attribute("date", new Date(2011,2,15)),
				attribute("start_time", new Time(8,32)),
				attribute("end_time", new Time(11,20))
			),
			entity("plot",
				attribute("no", new Code("1")),
				entity("tree",
					attribute("tree_no", 1),
					attribute("dbh", 54.2),
					attribute("total_height", 2.0),
					attribute("bole_height", (Double) null)
				),
				entity("tree",
					attribute("tree_no", 2),
					attribute("dbh", 82.8),
					attribute("total_height", 3.0)
				)
			),
			entity("plot",
				attribute("no", new Code("2")),
				entity("tree",
					attribute("tree_no", 1),
					attribute("dbh", 34.2),
					attribute("total_height", 2.0)
				),
				entity("tree",
					attribute("tree_no", 2),
					attribute("dbh", 85.8),
					attribute("total_height", 4.0)
				)
			)
		);
		CollectRecord record = recordBuilder.build(survey, "cluster", "2.0");
		User user = userManager.loadByUserName("admin");
		record.setCreatedBy(user);
		record.setModifiedBy(user);
		record.setCreationDate(new GregorianCalendar(2011, 11, 31, 23, 59).getTime());
		record.setModifiedDate(new GregorianCalendar(2012, 2, 3, 9, 30).getTime());
		record.setStep(Step.ENTRY);
		record.setState(State.REJECTED);
		record.updateSummaryFields();
	
		RecordUpdater recordUpdater = new RecordUpdater();
		recordUpdater.initializeRecord(record);
		
		Entity cluster = record.getRootEntity();
		recordUpdater.confirmError((Attribute<?, ?>) record.findNodeByPath("/cluster/district"));
		recordUpdater.approveMissingValue(cluster, "accessibility");
		NumberAttribute<?, ?> boleHeight = (NumberAttribute<?, ?>) record.findNodeByPath("/cluster/plot[1]/tree[1]/bole_height");
		recordUpdater.updateAttribute(boleHeight, FieldSymbol.BLANK_ON_FORM);
		recordUpdater.updateRemarks(boleHeight.getNumberField(), "No value specified");

		return record;
	}
}
