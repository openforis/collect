package org.openforis.collect.relational;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.GregorianCalendar;

import org.junit.BeforeClass;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * 
 * @author G. Miceli
 *
 */
public class CollectRelationalTest {

	protected static CollectSurvey survey;

	@BeforeClass
	public static void loadSurvey() throws IdmlParseException,
			FileNotFoundException {
					InputStream is = ClassLoader.getSystemResourceAsStream("test.idm.xml");
			//		InputStream is = new FileInputStream("/home/gino/workspace/of/idm/idm-test/src/main/resources/test.idm.xml");
			//		InputStream is = new FileInputStream("D:/data/workspace/idm/idm-test/src/main/resources/test.idm.xml");
			//		InputStream is = new FileInputStream("/home/gino/workspace/faofin/tz/naforma-idm/tanzania-naforma.idm.xml");
					CollectSurveyContext ctx = new CollectSurveyContext(new ExpressionFactory(), null);
			//		DefaultSurveyContext ctx = new DefaultSurveyContext();
					CollectSurveyIdmlBinder binder = new CollectSurveyIdmlBinder(ctx);
					survey = (CollectSurvey) binder.unmarshal(is);
				}

	protected static CollectRecord createTestRecord(CollectSurvey survey, String id) {
		CollectRecord record = new CollectRecord(survey, "2.0");
		Entity cluster = record.createRootEntity("cluster");
		record.setCreationDate(new GregorianCalendar(2011, 12, 31, 23, 59).getTime());
		//record.setCreatedBy("ModelDaoIntegrationTest");
		record.setStep(Step.ENTRY);
	
		addTestValues(cluster, id);
			
		//set counts
		record.getEntityCounts().add(2);
		
		//set keys
		record.getRootEntityKeyValues().add(id);
		
		return record;
	}

	private static void addTestValues(Entity cluster, String id) {
	//		cluster.setId(100);
			EntityBuilder.addValue(cluster, "id", new Code(id));
			EntityBuilder.addValue(cluster, "gps_realtime", Boolean.TRUE);
			EntityBuilder.addValue(cluster, "region", new Code("001"));
			EntityBuilder.addValue(cluster, "district", new Code("002"));
			EntityBuilder.addValue(cluster, "crew_no", 10);
			EntityBuilder.addValue(cluster, "map_sheet", "value 1");
			EntityBuilder.addValue(cluster, "map_sheet", "value 2");
			EntityBuilder.addValue(cluster, "vehicle_location", new Coordinate((double)432423423l, (double)4324324l, "srs"));
			EntityBuilder.addValue(cluster, "gps_model", "TomTom 1.232");
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
				boleHeight.getField(0).setSymbol('*');
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

	public CollectRelationalTest() {
		super();
	}

}