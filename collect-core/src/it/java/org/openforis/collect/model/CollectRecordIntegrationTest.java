/**
 * 
 */
package org.openforis.collect.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.Time;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class CollectRecordIntegrationTest extends CollectIntegrationTest {
	
	@Autowired
	private RecordManager recordManager;
	
	@Test
	public void testAddMultipleEntity() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		Entity cluster = record.getRootEntity();
		NodeChangeSet changeSet = recordManager.addEntity(cluster, "plot");
		assertNotNull(changeSet);
		assertEquals(2, changeSet.size());
		List<NodeChange<?>> changes = changeSet.getChanges();
		Iterator<NodeChange<?>> respIt = changes.iterator();
		{
			NodeChange<?> plotUpdateChange = respIt.next();
			assertTrue(plotUpdateChange instanceof NodeAddChange);
			assertTrue(plotUpdateChange instanceof EntityAddChange);
			Entity plot = ((EntityAddChange) plotUpdateChange).getNode();
			assertNotNull(plot);
			assertEquals("plot", plot.getName());
		}
		{
			NodeChange<?> clusterChange = respIt.next();
			assertTrue(clusterChange instanceof EntityChange);
			EntityChange clusterEntityChange = (EntityChange) clusterChange;
			Map<String, ValidationResultFlag> childrenMinCountValid = clusterEntityChange.getChildrenMinCountValidation();
			ValidationResultFlag plotMinCountValid = childrenMinCountValid.get("plot");
			assertEquals(ValidationResultFlag.OK, plotMinCountValid);
			Map<String, ValidationResultFlag> childrenMaxCountValid = clusterEntityChange.getChildrenMaxCountValidation();
			ValidationResultFlag plotMaxCountValid = childrenMaxCountValid.get("plot");
			assertEquals(ValidationResultFlag.OK, plotMaxCountValid);
		}
	}
	
	@Test
	public void testAddMultipleEntityWithMaxCount() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		Entity cluster = record.getRootEntity();
		{
			NodeChangeSet changeSet = recordManager.addEntity(cluster, "time_study");
			List<NodeChange<?>> changes = changeSet.getChanges();
			assertEquals(2, changes.size());
			Iterator<NodeChange<?>> respIt = changes.iterator();
			{
				NodeChange<?> timeStudyChange = respIt.next();
				assertTrue(timeStudyChange instanceof NodeAddChange);
				assertTrue(timeStudyChange instanceof EntityAddChange);
				Entity timeStudy = ((EntityAddChange) timeStudyChange).getNode();
				assertNotNull(timeStudy);
				assertEquals("time_study", timeStudy.getName());
			}
			{
				NodeChange<?> clusterChange = respIt.next();
				assertTrue(clusterChange instanceof EntityChange);
				EntityChange clusterEntityChange = (EntityChange) clusterChange;
				Map<String, ValidationResultFlag> childrenMinCountValid = clusterEntityChange.getChildrenMinCountValidation();
				ValidationResultFlag plotMinCountValid = childrenMinCountValid.get("time_study");
				assertEquals(ValidationResultFlag.OK, plotMinCountValid);
				Map<String, ValidationResultFlag> childrenMaxCountValid = clusterEntityChange.getChildrenMaxCountValidation();
				ValidationResultFlag plotMaxCountValid = childrenMaxCountValid.get("time_study");
				assertEquals(ValidationResultFlag.OK, plotMaxCountValid);
			}
		}
		{
			NodeChangeSet changeSet = recordManager.addEntity(cluster, "time_study");
			List<NodeChange<?>> changes = changeSet.getChanges();
			assertEquals(2, changes.size());
			NodeChange<?> clusterChange = changes.get(1);
			EntityChange clusterEntityChange = (EntityChange) clusterChange;
			Map<String, ValidationResultFlag> childrenMinCountValid = clusterEntityChange.getChildrenMinCountValidation();
			ValidationResultFlag plotMinCountValid = childrenMinCountValid.get("time_study");
			assertEquals(ValidationResultFlag.OK, plotMinCountValid);
			Map<String, ValidationResultFlag> childrenMaxCountValid = clusterEntityChange.getChildrenMaxCountValidation();
			ValidationResultFlag plotMaxCountValid = childrenMaxCountValid.get("time_study");
			assertEquals(ValidationResultFlag.ERROR, plotMaxCountValid);
		}
	}
	
	@Test
	public void testRemoveRequiredEntity() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		Entity cluster = record.getRootEntity();
		Entity timeStudy = (Entity) cluster.get("time_study", 0);
		NodeChangeSet changeSet = recordManager.deleteNode(timeStudy);
		List<NodeChange<?>> changes = changeSet.getChanges();
		assertEquals(2, changes.size());
		{
			NodeChange<?> timeStudyDeleteChange = changes.get(0);
			assertTrue(timeStudyDeleteChange instanceof NodeDeleteChange);
			Node<?> deletedNode = timeStudyDeleteChange.getNode();
			assertEquals(timeStudy.getInternalId(), deletedNode.getInternalId());
		}
		{
			NodeChange<?> clusterChange = changes.get(1);
			assertTrue(clusterChange instanceof EntityChange);
			EntityChange clusterEntityChange = (EntityChange) clusterChange;
			Map<String, ValidationResultFlag> childrenMinCountValid = clusterEntityChange.getChildrenMinCountValidation();
			ValidationResultFlag timeStudyMinCountValid = childrenMinCountValid.get("time_study");
			assertEquals(ValidationResultFlag.ERROR, timeStudyMinCountValid);
			Map<String, ValidationResultFlag> childrenMaxCountValid = clusterEntityChange.getChildrenMaxCountValidation();
			ValidationResultFlag timeStudyMaxCountValid = childrenMaxCountValid.get("time_study");
			assertEquals(ValidationResultFlag.OK, timeStudyMaxCountValid);
		}
	}
	
	@Test
	public void testApproveMissingValue() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		record.setStep(Step.CLEANSING);
		Entity cluster = record.getRootEntity();
		int missingCount = cluster.getMissingCount("time_study");
		assertEquals(0, missingCount);
		Entity timeStudy = (Entity) cluster.get("time_study", 0);
		{
			//delete node (min count error expected)
			NodeChangeSet changeSet = recordManager.deleteNode(timeStudy);
			List<NodeChange<?>> changes = changeSet.getChanges();
			int missingCount2 = cluster.getMissingCount("time_study");
			assertEquals(1, missingCount2);
			NodeChange<?> clusterChange = changes.get(1);
			assertTrue(clusterChange instanceof EntityChange);
			EntityChange clusterEntityChange = (EntityChange) clusterChange;
			Map<String, ValidationResultFlag> childrenMinCountValid = clusterEntityChange.getChildrenMinCountValidation();
			ValidationResultFlag timeStudyMinCountValid = childrenMinCountValid.get("time_study");
			assertEquals(ValidationResultFlag.ERROR, timeStudyMinCountValid);
		}
		{
			//approve missing value (min count warning expected)
			NodeChangeSet changeSet = recordManager.approveMissingValue(cluster, "time_study");
			List<NodeChange<?>> changes = changeSet.getChanges();
			NodeChange<?> clusterChange = changes.get(0);
			assertTrue(clusterChange instanceof EntityChange);
			assertEquals(cluster, clusterChange.getNode());
			EntityChange clusterEntityChange = (EntityChange) clusterChange;
			Map<String, ValidationResultFlag> childrenMinCountValid = clusterEntityChange.getChildrenMinCountValidation();
			ValidationResultFlag timeStudyMinCountValid = childrenMinCountValid.get("time_study");
			assertEquals(ValidationResultFlag.WARNING, timeStudyMinCountValid);
		}
	}
	
	@Test
	public void testConfirmError() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		Entity cluster = record.getRootEntity();
		CodeAttribute region = (CodeAttribute) cluster.get("region", 0);
		//add wrong value
		{
			NodeChangeSet changeSet = recordManager.updateAttribute(region, new Code("ZZZ"));
			assertFalse(changeSet.isEmpty());
			List<NodeChange<?>> changes = changeSet.getChanges();
			NodeChange<?> regionChange = changes.get(0);
			assertTrue(regionChange instanceof AttributeChange);
			ValidationResults validationResults = ((AttributeChange) regionChange).getValidationResults();
			List<ValidationResult> errors = validationResults.getErrors();
			assertFalse(errors.isEmpty());
			List<ValidationResult> warnings = validationResults.getWarnings();
			assertTrue(warnings.isEmpty());
		}
		{
			NodeChangeSet changeSet = recordManager.confirmError(region);
			assertFalse(changeSet.isEmpty());
			List<NodeChange<?>> changes = changeSet.getChanges();
			NodeChange<?> regionChange = changes.get(0);
			assertTrue(regionChange instanceof AttributeChange);
			ValidationResults validationResults = ((AttributeChange) regionChange).getValidationResults();
			List<ValidationResult> errors = validationResults.getErrors();
			assertTrue(errors.isEmpty());
			List<ValidationResult> warnings = validationResults.getWarnings();
			assertFalse(warnings.isEmpty());
		}
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
		/*
		{
			Entity ts = EntityBuilder.addEntity(cluster, "time_study");
			EntityBuilder.addValue(ts, "date", new Date(2011,2,15));
			EntityBuilder.addValue(ts, "start_time", new Time(8,32));
			EntityBuilder.addValue(ts, "end_time", new Time(11,20));
		}
		*/
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
			
			{
				Entity humanImpact = EntityBuilder.addEntity(plot, "human_impact");
				EntityBuilder.addValue(humanImpact, "type", new Code("0"));
			}
		}
	}
}
