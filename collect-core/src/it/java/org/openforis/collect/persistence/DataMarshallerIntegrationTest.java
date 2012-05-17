package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshallerException;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class DataMarshallerIntegrationTest {
	
	@Autowired
	protected SurveyDao surveyDao;
	
	@Autowired
	private ExpressionFactory expressionFactory;
	
	@Autowired
	private Validator validator;
	
	@Autowired
	private DataMarshaller dataMarshaller;
	
	@Test
	public void testMarshal() throws Exception  {
		// LOAD MODEL
		CollectSurvey survey = surveyDao.load("archenland1");

		if ( survey == null ) {
			// IMPORT MODEL
			survey = importModel();
		}
		
		CollectRecord record = createTestRecord(survey);
		
		StringWriter out = new StringWriter();

		dataMarshaller.write(record, out);
		String xml = out.toString();
		assertNotNull(xml);
		
		CollectRecord record2 = parseRecord(survey, xml);

		assertNotNull(record2);
		
		Entity rootEntity1 = record.getRootEntity();
		
		Entity rootEntity2 = record2.getRootEntity();		
			
		assertNotNull(rootEntity2);
		
		testSingleAttributesEqual(rootEntity1, rootEntity2, "region");
		testSingleAttributesEqual(rootEntity1, rootEntity2, "crew_no");
		testSingleAttributesEqual(rootEntity1, rootEntity2, "vehicle_location");
		
		testMultipleAttributesEqual(rootEntity1, rootEntity2, "map_sheet");
		
		Entity plot1 = (Entity) rootEntity1.get("plot", 0);
		Entity plot2 = (Entity) rootEntity2.get("plot", 0);
		
		assertNotNull(plot2);
		
		testSingleAttributesEqual(plot1, plot2, "no");
		
		Entity tree1 = (Entity) plot1.get("tree", 0);
		Entity tree2 = (Entity) plot2.get("tree", 0);
		
		assertNotNull(tree2);
		
		testSingleAttributesEqual(tree1, tree2, "dbh");
		testSingleAttributesEqual(tree1, tree2, "bole_height");
	}
	
	private void testMultipleAttributesEqual(Entity rootEntity1, Entity rootEntity2, String attributeName) {
		List<Node<?>> attributes1 = rootEntity1.getAll(attributeName);
		List<Node<?>> attributes2 = rootEntity2.getAll(attributeName);
		assertEquals(attributes1.size(), attributes2.size());
		
		for (int i = 0; i < attributes1.size(); i++) {
			Attribute<?, ?> a1 = (Attribute<?, ?>) attributes1.get(i);
			Attribute<?, ?> a2 = (Attribute<?, ?>) attributes2.get(i);
			testAttributesEqual(a1, a2);
		}
	}

	private void testSingleAttributesEqual(Entity rootEntity1, Entity rootEntity2, String attributeName) {
		Attribute<?, ?> attribute1 = (Attribute<?, ?>) rootEntity1.get(attributeName, 0);
		Attribute<?, ?> attribute2 = (Attribute<?, ?>) rootEntity2.get(attributeName, 0);
		assertNotNull(attribute1);
		assertNotNull(attribute2);
		testAttributesEqual(attribute1, attribute2);
	}

	private void testAttributesEqual(Attribute<?, ?> attribute1, Attribute<?, ?> attribute2) {
		int fieldCount = attribute1.getFieldCount();
		for (int i = 0; i < fieldCount; i++) {
			Field<?> field1 = attribute1.getField(i);
			Field<?> field2 = attribute2.getField(i);
			assertEquals(field1.getValue(), field2.getValue());
			assertEquals(field1.getRemarks(), field2.getRemarks());
			assertEquals(field1.getSymbol(), field2.getSymbol());
			assertEquals(field1.getState().intValue(), field2.getState().intValue());
		}
	}

	private CollectRecord parseRecord(CollectSurvey survey, String xml) throws IOException, DataUnmarshallerException {
		DataHandler dataHandler = new DataHandler(survey);
		DataUnmarshaller dataUnmarshaller = new DataUnmarshaller(dataHandler);
		StringReader reader = new StringReader(xml);
		CollectRecord parsedRecord = dataUnmarshaller.parse(reader);
		return parsedRecord;
	}
	
	private CollectRecord createTestRecord(CollectSurvey survey) {
		CollectRecord record = new CollectRecord(survey, "2.0");
		Entity cluster = record.createRootEntity("cluster");
		record.setCreationDate(new GregorianCalendar(2011, 12, 31, 23, 59).getTime());
		//record.setCreatedBy("ModelDaoIntegrationTest");
		record.setStep(Step.ENTRY);

		addTestValues(cluster);

		//update counts and keys
		record.updateRootEntityKeyValues();
		record.updateEntityCounts();
		
		return record;
	}
	
	private void addTestValues(Entity cluster) {
		CollectRecord record = (CollectRecord) cluster.getRecord();
		cluster.addValue("id", new Code("123_456"));
		cluster.addValue("gps_realtime", Boolean.TRUE);
		cluster.addValue("region", new Code("001"));
		CodeAttribute districtAttr = cluster.addValue("district", new Code("XXX"));
		record.setErrorConfirmed(districtAttr, true);
		cluster.addValue("crew_no", 10);
		cluster.addValue("map_sheet", "value 1");
		cluster.addValue("map_sheet", "value 2");
		cluster.addValue("vehicle_location", new Coordinate((double)432423423l, (double)4324324l, "srs"));
		cluster.addValue("gps_model", "TomTom 1.232");
		cluster.setChildState("accessibility", 1);
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
			boleHeight.getField(0).setSymbol(FieldSymbol.BLANK_ON_FORM.getCode());
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
	
	private CollectSurvey importModel() throws IOException, SurveyImportException, InvalidIdmlException {
		URL idm = ClassLoader.getSystemResource("test.idm.xml");
		InputStream is = idm.openStream();
		CollectSurveyContext surveyContext = new CollectSurveyContext(expressionFactory, validator, null);
		CollectIdmlBindingContext idmlBindingContext = new CollectIdmlBindingContext(surveyContext);
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		survey.setName("archenland1");
		surveyDao.importModel(survey);
		return survey;
	}

}
