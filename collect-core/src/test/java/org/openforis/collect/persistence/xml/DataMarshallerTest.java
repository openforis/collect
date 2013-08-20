package org.openforis.collect.persistence.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataMarshallerTest extends CollectIntegrationTest {
	
	private DataMarshaller dataMarshaller;
	@Autowired
	private RecordManager recordManager;
	
	@Before
	public void init() {
		dataMarshaller = new DataMarshaller();
	}
	
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
		Document doc = parseXml(xml);
		
		Object res = evaluateXPathExpression(doc, "cluster/id/code");
		assertEquals("123_456", res);
		res = evaluateXPathExpression(doc, "cluster/time_study[1]/start_time/minute");
		assertEquals("15", res);
		res = evaluateXPathExpression(doc, "cluster/time_study[1]/start_time/minute/@symbol");
		assertEquals("", res);
		res = evaluateXPathExpression(doc, "cluster/time_study[2]/start_time/minute");
		assertEquals("32", res);
		res = evaluateXPathExpression(doc, "cluster/plot[1]/tree[1]/bole_height/value/@remarks");
		assertEquals("No value specified", res);
		res = evaluateXPathExpression(doc, "cluster/plot[1]/tree[1]/bole_height/value/@symbol");
		assertEquals(Character.toString(FieldSymbol.BLANK_ON_FORM.getCode()), res);

		//test blank values
		res = evaluateXPathExpression(doc, "cluster/id/code/@remarks");
		assertEquals("", res);
		res = evaluateXPathExpression(doc, "cluster/id/code/@symbol");
		assertEquals("", res);
		
		//test child state (confirmed error)
		res = evaluateXPathExpression(doc, "cluster/district/code/@state");
		assertEquals("1", res);
		//test child state (approved missing value)
		res = evaluateXPathExpression(doc, "cluster/accessibility/@state");
		assertEquals("1", res);
		//test blank child state
		res = evaluateXPathExpression(doc, "cluster/id/@state");
		assertEquals("", res);
	}
	
	private CollectRecord createTestRecord(CollectSurvey survey) {
		CollectRecord record = new CollectRecord(survey, "2.0");
		Entity cluster = record.createRootEntity("cluster");
		record.setCreationDate(new GregorianCalendar(2011, 11, 31, 23, 59).getTime());
		//record.setCreatedBy("ModelDaoIntegrationTest");
		record.setStep(Step.ENTRY);

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
		cluster.setChildState("accessibility", 1);
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
	
	private Document parseXml(String xml) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		Document doc = db.parse(is);
		return doc;
	}

	private Object evaluateXPathExpression(Document doc, String expression) throws XPathExpressionException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile(expression);
		String result = expr.evaluate(doc);
		return result;
	}
	
}
