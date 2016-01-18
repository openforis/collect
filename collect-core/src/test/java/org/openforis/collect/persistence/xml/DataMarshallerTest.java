package org.openforis.collect.persistence.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.openforis.idm.testfixture.NodeBuilder.attribute;
import static org.openforis.idm.testfixture.NodeBuilder.entity;
import static org.openforis.idm.testfixture.RecordBuilder.record;

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
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.Time;
import org.openforis.idm.testfixture.RecordBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataMarshallerTest extends CollectIntegrationTest {
	
	private DataMarshaller dataMarshaller;
	
	@Before
	public void init() {
		dataMarshaller = new DataMarshaller();
	}
	
	@Test
	public void testMarshal() throws Exception  {
		// LOAD MODEL
		CollectSurvey survey = surveyManager.get("archenland1");

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
	
	private CollectRecord createTestRecord(CollectSurvey survey) throws RecordPersistenceException {
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
		record.setCreationDate(new GregorianCalendar(2011, 11, 31, 23, 59).getTime());
		record.setStep(Step.ENTRY);
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
