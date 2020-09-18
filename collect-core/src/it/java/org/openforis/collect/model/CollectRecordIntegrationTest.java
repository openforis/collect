/**
 * 
 */
package org.openforis.collect.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.validation.CodeParentValidator;
import org.openforis.idm.metamodel.validation.CodeValidator;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.Time;

/**
 * @author S. Ricci
 *
 */
public class CollectRecordIntegrationTest extends CollectIntegrationTest {

	private RecordUpdater recordUpdater;
	
	@Before
	public void init() {
		recordUpdater = new RecordUpdater();
	}
	
	@Test
	public void testAddMultipleEntity() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		Entity cluster = record.getRootEntity();
		NodeChangeSet changeSet = recordUpdater.addEntity(cluster, "plot");
		assertNotNull(changeSet);
		assertEquals(81, changeSet.size());
		assertEquals(3, cluster.getCount("plot"));
		{
			Entity plot = (Entity) cluster.getChild("plot", 2);
			NodeChange<?> plotUpdateChange = changeSet.getChange(plot);
			assertTrue(plotUpdateChange instanceof EntityAddChange);
		}
	}
	
	@Test
	public void testAddMultipleEntityWithMaxCount() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		Entity cluster = record.getRootEntity();
		{
			EntityDefinition entityDefinition = (EntityDefinition) cluster.getDefinition().getChildDefinition("time_study");
			int entityDefinitionId = entityDefinition.getId();
			NodeChangeSet changeSet = recordUpdater.addEntity(cluster, entityDefinition);
			assertEquals(4, changeSet.size());
			
			changeSet = recordUpdater.addEntity(cluster, entityDefinition);
			assertEquals(5, changeSet.size());
			{
				Entity timeStudy = (Entity) cluster.getChild(entityDefinition, 2);
				NodeChange<?> timeStudyChange = changeSet.getChange(timeStudy); 
				assertTrue(timeStudyChange instanceof EntityAddChange);
			}
			{
				NodeChange<?> clusterChange = changeSet.getChange(cluster);
				assertTrue(clusterChange instanceof EntityChange);
				EntityChange clusterEntityChange = (EntityChange) clusterChange;
				Map<Integer, ValidationResultFlag> childrenMinCountValid = clusterEntityChange.getChildrenMinCountValidation();
				ValidationResultFlag plotMinCountValid = childrenMinCountValid.get(entityDefinitionId);
				assertNull(plotMinCountValid);
				Map<Integer, ValidationResultFlag> childrenMaxCountValid = clusterEntityChange.getChildrenMaxCountValidation();
				ValidationResultFlag plotMaxCountValid = childrenMaxCountValid.get(entityDefinitionId);
				assertEquals(ValidationResultFlag.ERROR, plotMaxCountValid);
			}
		}
	}
	
	@Test
	public void testRemoveRequiredEntity() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		Entity cluster = record.getRootEntity();
		EntityDefinition entityDefinition = (EntityDefinition) cluster.getDefinition().getChildDefinition("time_study");
		int entityDefinitionId = entityDefinition.getId();
		Entity timeStudy = (Entity) cluster.getChild(entityDefinition, 0);
		
		NodeChangeSet changeSet = recordUpdater.deleteNode(timeStudy);
		assertEquals(2, changeSet.size());
		{
			NodeChange<?> timeStudyDeleteChange = changeSet.getChange(timeStudy);
			assertTrue(timeStudyDeleteChange instanceof NodeDeleteChange);
			Node<?> deletedNode = timeStudyDeleteChange.getNode();
			assertEquals(timeStudy.getInternalId(), deletedNode.getInternalId());
		}
		{
			NodeChange<?> clusterChange = changeSet.getChange(cluster);
			assertTrue(clusterChange instanceof EntityChange);
			EntityChange clusterEntityChange = (EntityChange) clusterChange;
			Map<Integer, ValidationResultFlag> childrenMinCountValid = clusterEntityChange.getChildrenMinCountValidation();
			ValidationResultFlag timeStudyMinCountValid = childrenMinCountValid.get(entityDefinitionId);
			assertEquals(ValidationResultFlag.ERROR, timeStudyMinCountValid);
			Map<Integer, ValidationResultFlag> childrenMaxCountValid = clusterEntityChange.getChildrenMaxCountValidation();
			ValidationResultFlag timeStudyMaxCountValid = childrenMaxCountValid.get(entityDefinitionId);
			assertNull(timeStudyMaxCountValid);
		}
	}
	
	@Test
	public void testApproveMissingValue() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		record.setStep(Step.CLEANSING);
		Entity cluster = record.getRootEntity();
		String entityName = "time_study";
		EntityDefinition entityDefinition = (EntityDefinition) cluster.getDefinition().getChildDefinition(entityName);
		int missingCount = cluster.getMissingCount(entityDefinition);
		assertEquals(0, missingCount);
		Entity timeStudy = (Entity) cluster.getChild(entityDefinition, 0);
		int entityDefinitionId = entityDefinition.getId();
		{
			//delete node (min count error expected)
			NodeChangeSet changeSet = recordUpdater.deleteNode(timeStudy);
			int missingCount2 = cluster.getMissingCount(entityName);
			assertEquals(1, missingCount2);
			NodeChange<?> clusterChange = changeSet.getChange(cluster);
			assertTrue(clusterChange instanceof EntityChange);
			EntityChange clusterEntityChange = (EntityChange) clusterChange;
			Map<Integer, ValidationResultFlag> childrenMinCountValid = clusterEntityChange.getChildrenMinCountValidation();
			ValidationResultFlag timeStudyMinCountValid = childrenMinCountValid.get(entityDefinitionId);
			assertEquals(ValidationResultFlag.ERROR, timeStudyMinCountValid);
		}
		{
			//approve missing value (min count warning expected)
			NodeChangeSet changeSet = recordUpdater.approveMissingValue(cluster, entityName);
			NodeChange<?> clusterChange = changeSet.getChange(cluster);
			assertTrue(clusterChange instanceof EntityChange);
			assertEquals(cluster, clusterChange.getNode());
			EntityChange clusterEntityChange = (EntityChange) clusterChange;
			Map<Integer, ValidationResultFlag> childrenMinCountValid = clusterEntityChange.getChildrenMinCountValidation();
			ValidationResultFlag timeStudyMinCountValid = childrenMinCountValid.get(entityDefinitionId);
			assertEquals(ValidationResultFlag.WARNING, timeStudyMinCountValid);
		}
	}
	
	@Test
	public void testUpdateValue() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		Entity cluster = record.getRootEntity();
		CodeAttribute region = (CodeAttribute) cluster.getChild("region", 0);
		{
			NodeChangeSet nodeChangeSet = recordUpdater.updateAttribute(region, FieldSymbol.BLANK_ON_FORM);
			assertTrue(region.isEmpty());
			assertEquals(FieldSymbol.BLANK_ON_FORM, FieldSymbol.valueOf(region.getCodeField().getSymbol()));
			assertEquals(FieldSymbol.BLANK_ON_FORM, FieldSymbol.valueOf(region.getQualifierField().getSymbol()));

			NodeChange<?> regionChange = nodeChangeSet.getChange(region);
			assertTrue(regionChange instanceof AttributeChange);
			assertEquals(region, regionChange.getNode());
			Map<Integer, Object> updatedFieldValues = ((AttributeChange) regionChange).getUpdatedFieldValues();
			Map<Integer, Object> expectedValues = new HashMap<Integer, Object>();
			expectedValues.put(Integer.valueOf(0), null);
			expectedValues.put(Integer.valueOf(1), null);
			assertEquals(expectedValues, updatedFieldValues);
		}
		{
			NodeChangeSet nodeChangeSet = recordUpdater.updateAttribute(region, new Code("AAA"));
			assertFalse(region.isEmpty());
			assertEquals(null, FieldSymbol.valueOf(region.getCodeField().getSymbol()));
			assertEquals(null, FieldSymbol.valueOf(region.getQualifierField().getSymbol()));

			NodeChange<?> regionChange = nodeChangeSet.getChange(region);
			assertTrue(regionChange instanceof AttributeChange);
			assertEquals(region, regionChange.getNode());
			Map<Integer, Object> updatedFieldValues = ((AttributeChange) regionChange).getUpdatedFieldValues();
			Map<Integer, Object> expectedValues = new HashMap<Integer, Object>();
			expectedValues.put(Integer.valueOf(0), "AAA");
			expectedValues.put(Integer.valueOf(1), null);
			assertEquals(expectedValues, updatedFieldValues);
		}
	}
	
	@Test
	public void testConfirmError() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		Entity cluster = record.getRootEntity();
		CodeAttribute region = (CodeAttribute) cluster.getChild("region", 0);
		//add wrong value
		{
			NodeChangeSet changeSet = recordUpdater.updateAttribute(region, new Code("ZZZ"));
			assertFalse(changeSet.isEmpty());
			NodeChange<?> regionChange = changeSet.getChange(region);
			assertTrue(regionChange instanceof AttributeChange);
			ValidationResults validationResults = ((AttributeChange) regionChange).getValidationResults();
			List<ValidationResult> errors = validationResults.getErrors();
			assertFalse(errors.isEmpty());
			List<ValidationResult> warnings = validationResults.getWarnings();
			assertTrue(warnings.isEmpty());
		}
		{
			NodeChangeSet changeSet = recordUpdater.confirmError(region);
			assertFalse(changeSet.isEmpty());
			NodeChange<?> regionChange = changeSet.getChange(region);
			assertTrue(regionChange instanceof AttributeChange);
			ValidationResults validationResults = ((AttributeChange) regionChange).getValidationResults();
			List<ValidationResult> errors = validationResults.getErrors();
			assertTrue(errors.isEmpty());
			List<ValidationResult> warnings = validationResults.getWarnings();
			assertFalse(warnings.isEmpty());
		}
	}

	@Test
	public void testMultipleCodeListLevelValidation() {
		CollectRecord record = createTestMultipleCodeListLevelRecord();
		assertEquals(Integer.valueOf(0), record.getErrors());
		assertEquals(Integer.valueOf(0), record.getWarnings());
		
		Entity rootEntity = record.getRootEntity();
		
		CodeAttribute code1 = (CodeAttribute) rootEntity.getChild("code1", 0);
		CodeAttribute code2 = (CodeAttribute) rootEntity.getChild("code2", 0);
		CodeAttribute code3 = (CodeAttribute) rootEntity.getChild("code3", 0);
		
		recordUpdater.updateAttribute(code1, new Code("WRONG"));
		
		assertEquals(Integer.valueOf(1), record.getErrors());
		assertEquals(Integer.valueOf(2), record.getWarnings());
		
		checkHasError(record, code1.getInternalId(), CodeValidator.class);
		checkHasWarning(record, code2.getInternalId(), CodeParentValidator.class);
		checkHasWarning(record, code3.getInternalId(), CodeParentValidator.class);
		
		recordUpdater.updateAttribute(code1, new Code("A"));
		
		assertEquals(Integer.valueOf(0), record.getErrors());
		assertEquals(Integer.valueOf(0), record.getWarnings());
		
		recordUpdater.updateAttribute(code2, new Code("WRONG"));
		
		assertEquals(Integer.valueOf(1), record.getErrors());
		assertEquals(Integer.valueOf(1), record.getWarnings());
			
		checkHasError(record, code2.getInternalId(), CodeValidator.class);
		checkHasWarning(record, code3.getInternalId(), CodeParentValidator.class);
	}

	private void checkHasError(CollectRecord record, int attributeId, Class<?> checkType) {
		ValidationResults validationResults = record.getValidationCache().getAttributeValidationResults(attributeId);
		List<ValidationResult> errors = validationResults.getErrors();
		for (ValidationResult validationResult : errors) {
			if ( checkType.isAssignableFrom(validationResult.getValidator().getClass()) ) {
				return;
			}
		}
		Assert.fail("Error of class " + checkType + " not found in validation results");
	}
	
	private void checkHasWarning(CollectRecord record, int attributeId, Class<?> checkType) {
		ValidationResults validationResults = record.getValidationCache().getAttributeValidationResults(attributeId);
		List<ValidationResult> warnings = validationResults.getWarnings();
		for (ValidationResult validationResult : warnings) {
			if ( checkType.isAssignableFrom(validationResult.getValidator().getClass()) ) {
				return;
			}
		}
		Assert.fail();
	}
	
	private CollectRecord createTestRecord(CollectSurvey survey) throws RecordPersistenceException {
		CollectRecord record = survey.createRecord("2.0", "cluster");
		String id = "123_456";
		addTestValues(record.getRootEntity(), id);
		record.setRootEntityKeyValues(Arrays.asList(id));
		record.setEntityCounts(Arrays.asList(2));

		recordUpdater.initializeRecord(record);
		return record;
	}
	
	private CollectSurvey createMultipleLevelCodeListTestSurvey() {
		CollectSurvey survey = surveyManager.createTemporarySurvey("test", "en");
		CodeList codeList = survey.createCodeList();
		{
			CodeListLevel codeListLevel = new CodeListLevel();
			codeListLevel.setName("level1");
			codeList.addLevel(codeListLevel);
		}
		{
			CodeListLevel codeListLevel = new CodeListLevel();
			codeListLevel.setName("level2");
			codeList.addLevel(codeListLevel);
		}
		{
			CodeListLevel codeListLevel = new CodeListLevel();
			codeListLevel.setName("level3");
			codeList.addLevel(codeListLevel);
		}
		{
			CodeListItem item  = codeList.createItem(1);
			item.setCode("A");
			codeList.addItem(item);
			{
				CodeListItem child = codeList.createItem(2);
				child.setCode("1");
				item.addChildItem(child);
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("a");
					child.addChildItem(child2);
				}
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("b");
					child.addChildItem(child2);
				}
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("c");
					child.addChildItem(child2);
				}
			}
			{
				CodeListItem child = codeList.createItem(2);
				child.setCode("2");
				item.addChildItem(child);
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("a");
					child.addChildItem(child2);
				}
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("b");
					child.addChildItem(child2);
				}
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("c");
					child.addChildItem(child2);
				}
			}
			{
				CodeListItem child = codeList.createItem(2);
				child.setCode("3");
				item.addChildItem(child);
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("a");
					child.addChildItem(child2);
				}
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("b");
					child.addChildItem(child2);
				}
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("c");
					child.addChildItem(child2);
				}
			}
		}
		{
			CodeListItem item  = codeList.createItem(1);
			item.setCode("B");
			codeList.addItem(item);
			{
				CodeListItem child = codeList.createItem(2);
				child.setCode("1");
				item.addChildItem(child);
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("a");
					child.addChildItem(child2);
				}
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("b");
					child.addChildItem(child2);
				}
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("c");
					child.addChildItem(child2);
				}
			}
			{
				CodeListItem child = codeList.createItem(2);
				child.setCode("2");
				item.addChildItem(child);
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("a");
					child.addChildItem(child2);
				}
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("b");
					child.addChildItem(child2);
				}
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("c");
					child.addChildItem(child2);
				}
			}
			{
				CodeListItem child = codeList.createItem(2);
				child.setCode("3");
				item.addChildItem(child);
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("a");
					child.addChildItem(child2);
				}
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("b");
					child.addChildItem(child2);
				}
				{
					CodeListItem child2 = codeList.createItem(3);
					child2.setCode("c");
					child.addChildItem(child2);
				}
			}
		}
		survey.addCodeList(codeList);
		
		Schema schema = survey.getSchema();
		EntityDefinition root = schema.createEntityDefinition();
		root.setName("root");
		schema.addRootEntityDefinition(root);
		{
			CodeAttributeDefinition codeAttrDefn = schema.createCodeAttributeDefinition();
			codeAttrDefn.setList(codeList);
			codeAttrDefn.setName("code1");
			root.addChildDefinition(codeAttrDefn);
		}
		{
			CodeAttributeDefinition codeAttrDefn = schema.createCodeAttributeDefinition();
			codeAttrDefn.setList(codeList);
			codeAttrDefn.setName("code2");
			codeAttrDefn.setParentExpression("code1");
			root.addChildDefinition(codeAttrDefn);
		}
		{
			CodeAttributeDefinition codeAttrDefn = schema.createCodeAttributeDefinition();
			codeAttrDefn.setList(codeList);
			codeAttrDefn.setName("code3");
			codeAttrDefn.setParentExpression("code2");
			root.addChildDefinition(codeAttrDefn);
		}
		survey.init();
		return survey;
	}
	
	@SuppressWarnings("unchecked")
	private CollectRecord createTestMultipleCodeListLevelRecord() {
		CollectSurvey survey = createMultipleLevelCodeListTestSurvey();
		CollectRecord record = new CollectRecord(survey, null, "root");
		record.setCreationDate(new GregorianCalendar(2011, 11, 31, 23, 59).getTime());
		record.setStep(Step.ENTRY);
		recordUpdater.initializeRecord(record);
		recordUpdater.updateAttribute((Attribute<?, Code>) record.findNodeByPath("root/code1"), new Code("A"));
		recordUpdater.updateAttribute((Attribute<?, Code>) record.findNodeByPath("root/code2"), new Code("2"));
		recordUpdater.updateAttribute((Attribute<?, Code>) record.findNodeByPath("root/code3"), new Code("b"));
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
