package org.openforis.collect.persistence;

import static org.openforis.idm.testfixture.NodeBuilder.attribute;
import static org.openforis.idm.testfixture.NodeBuilder.entity;
import static org.openforis.idm.testfixture.RecordBuilder.record;

import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelSerializer;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.Time;
import org.openforis.idm.testfixture.RecordBuilder;

public class ModelSerializationTest extends CollectIntegrationTest {

//	private final Logger log = Logger.getLogger(ModelSerializationTest.class);
	
	private RecordUpdater updater;
	
	@Before
	public void init() {
		updater = new RecordUpdater();
	}
	
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
		CollectRecord record2 = new CollectRecord(survey, "2.0", "cluster");
		Entity reloadedEntity = record2.getRootEntity();
		ms.mergeFrom(data, reloadedEntity);
		
		Assert.assertEquals(entity, reloadedEntity);
	}
	
	private CollectRecord createTestRecord(CollectSurvey survey) {
		RecordBuilder recordBuilder = record(
			attribute("id", new Code("123_456")),
			attribute("gps_realtime", "true"),
			attribute("region", "001"),
			attribute("district", "002"),
			attribute("crew_no", 10),
			attribute("map_sheet", "value 1"),
			attribute("map_sheet", "value 2"),
			attribute("vehicle_location", new Coordinate(432423423d, 4324324d, "srs")),
			attribute("gps_model", "TomTom 1.232"),
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
		record.setCreationDate(new GregorianCalendar(2011, 11, 31, 23, 59).getTime());
//			record.setCreatedBy("ModelDaoIntegrationTest");
		record.setStep(Step.ENTRY);
		record.updateSummaryFields();
	
		updater.initializeRecord(record);
		
		Entity cluster = record.getRootEntity();
		updater.confirmError((Attribute<?, ?>) record.findNodeByPath("/cluster/district"));
		updater.approveMissingValue(cluster, "accessibility");
		NumberAttribute<?, ?> boleHeight = (NumberAttribute<?, ?>) record.findNodeByPath("/cluster/plot[1]/tree[1]/bole_height");
		updater.updateAttribute(boleHeight, FieldSymbol.BLANK_ON_FORM);
		updater.updateRemarks(boleHeight.getNumberField(), "No value specified");
		
		return record;
	}
}
