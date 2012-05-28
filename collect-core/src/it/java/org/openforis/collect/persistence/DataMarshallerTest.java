package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class DataMarshallerTest {
	
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
		
		//test definition id
		res = evaluateXPathExpression(doc, "cluster/id/@defn");
		Schema schema = survey.getSchema();
		NodeDefinition nodeDefn = schema.getByPath("/cluster/id");
		Integer nodeDefnId = nodeDefn.getId();
		assertEquals(nodeDefnId.toString(), res);
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
