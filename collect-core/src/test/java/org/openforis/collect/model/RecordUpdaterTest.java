/**
 * 
 */
package org.openforis.collect.model;

import static org.junit.Assert.*;
import static org.openforis.collect.model.NodeBuilder.*;
import static org.openforis.idm.metamodel.NodeDefinitionBuilder.*;

import org.junit.Before;
import org.junit.Test;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinitionBuilder;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Value;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
@SuppressWarnings("unchecked")
public class RecordUpdaterTest {

	private RecordUpdater updater;
	private Survey survey;
	private Record record;

	@Before
	public void init() {
		survey = createTestSurvey();
		updater = new RecordUpdater();
	}
	
	@Test
	public void testThisVariableCanReturnNodes() {
		record(
			rootEntityDef(
				entityDef("tree",
					attributeDef("tree_count")
						.calculated("idm:position($this/parent())")
				).multiple()
			)
		);
		
		updater.addEntity(record.getRootEntity(), "tree");
		
		Attribute<?, ?> treeCount = findAttribute("/root/tree[1]/tree_count");
		assertEquals(new TextValue("1"), treeCount.getValue());
	}

	protected EntityDefinition rootEntityDef(NodeDefinitionBuilder... builders) {
		EntityDefinition rootEntityDef = NodeDefinitionBuilder.rootEntityDef(survey, "root", builders);
		return rootEntityDef;
	}

	@Test
	public void testUpdateAttribute() {
		record(
			rootEntityDef(
				attributeDef("attribute")
			),
			attribute("attribute", "initial value")
		);
		
		Attribute<?,?> attr = findAttribute("root/attribute[1]");
		
		NodeChangeSet result = update(attr, "new value");
		
		assertNotNull(result);
		assertEquals(1, result.size());
		
		AttributeChange attrChange = (AttributeChange) result.getChange(attr);
		
		assertNotNull(attrChange);
	}

	@Test
	public void testCardinalityValidatedOnRecordInitialization() {
		record(
			rootEntityDef(
				entityDef("time_study",
					attributeDef("start_time")
				)
				.multiple()
				.required()
			)
		);
		Entity rootEntity = record.getRootEntity();
		assertEquals(ValidationResultFlag.ERROR, rootEntity.getMinCountValidationResult("time_study"));
	}
	
	@Test
	public void testCardinalityValidatedOnAttributeUpdate() {
		record(
			rootEntityDef(
				entityDef("time_study",
					attributeDef("start_time")
				)
				.multiple()
				.required()
			)
		);
		Entity rootEntity = record.getRootEntity();
		Attribute<?, TextValue> startTime = (Attribute<?, TextValue>) record.findNodeByPath("/root/time_study[1]/start_time");
		
		NodeChangeSet nodeChangeSet = updater.updateAttribute(startTime, new TextValue("updated"));
		
		assertNotNull(nodeChangeSet.getChange(startTime));
		assertNotNull(nodeChangeSet.getChange(rootEntity));
		assertEquals(ValidationResultFlag.OK, rootEntity.getMinCountValidationResult("time_study"));
	}
	
	@Test
	public void testRecordInitializationPreservesEntities() {
		record(
			rootEntityDef(
				entityDef("time_study",
					attributeDef("start_time")
				)
				.multiple()
				.required()
			),
			
			entity("time_study", 
				attribute("start_time", "start first")
			),
			entity("time_study", 
				attribute("start_time", "start second")
			)
		);
		Entity rootEntity = record.getRootEntity();
		
		assertEquals(2, rootEntity.getCount("time_study"));
	}
	
	@Test
	public void testRemoveEntity() {
		record(
			rootEntityDef(
				entityDef("time_study",
					attributeDef("start_time")
				)
				.multiple()
				.required()
			),
			entity("time_study", 
				attribute("start_time", "start first")
			),
			entity("time_study", 
				attribute("start_time", "start second")
			)
		);
		Entity timeStudy1 = entityByPath("/root/time_study[1]");
		updater.deleteNode(timeStudy1);

		Entity rootEntity = record.getRootEntity();
		
		assertEquals(1, rootEntity.getCount("time_study"));
		Entity timeStudy2 = (Entity) rootEntity.get("time_study", 0);
		Attribute<?, ?> startTime2 = (Attribute<?, ?>) timeStudy2.getChild("start_time");
		
		assertEquals(new TextValue("start second"), startTime2.getValue());
	}

	@Test
	public void testRemoveEntityGivesNodeDeleteChange() {
		record(
			rootEntityDef(
				entityDef("time_study",
					attributeDef("start_time")
				)
				.multiple()
				.required()
			),
			entity("time_study", 
				attribute("start_time", "start first")
			),
			entity("time_study", 
				attribute("start_time", "start second")
			)
		);
		Entity timeStudy1 = entityByPath("/root/time_study[1]");
		NodeChangeSet changeSet = updater.deleteNode(timeStudy1);
		
		assertEquals(1, changeSet.size());
		
		NodeChange<?> change = changeSet.getChange(timeStudy1);
		assertTrue(change instanceof NodeDeleteChange);
	}
	
	@Test
	public void testRemoveEntityWithCalculatedAttribute() {
		record(
			rootEntityDef(
				entityDef("plot_details",
					attributeDef("dbh_sum")
						.calculated("sum(parent()/tree/dbh)"),
					attributeDef("tree_health")
						.relevant("dbh_sum > 0")
						.required()
				),
				entityDef("tree",
					attributeDef("dbh")
				).multiple()
			),
			entity("plot_details", 
				attribute("dbh_sum"),
				attribute("tree_health")
			),
			entity("tree", 
				attribute("dbh", "1")
			)
		);
		Entity plotDetails = entityByPath("/root/plot_details");
		Entity tree1 = entityByPath("/root/tree[1]");
		
		NodeChangeSet changeSet = updater.deleteNode(tree1);
		
		Attribute<?, ?> dbhSum = (Attribute<?, ?>) plotDetails.getChild("dbh_sum");
		assertEquals(new TextValue("0.0"), dbhSum.getValue());
		
		NodeChange<?> dbhSumChange = changeSet.getChange(dbhSum);
		assertNotNull(dbhSumChange);
	}
	
	@Test
	public void testRemoveEntityUpdatesCalculatedPosition() {
		record(
			rootEntityDef(
				entityDef("tree",
					attributeDef("tree_num")
						.calculated("idm:position()")
				)
				.multiple()
			),
			entity("tree"),
			entity("tree"),
			entity("tree")
		);
		Entity tree1 = entityByPath("/root/tree[1]");
		Entity tree2 = entityByPath("/root/tree[2]");
		Entity tree3 = entityByPath("/root/tree[3]");
		
		updater.deleteNode(tree2);
		
		Attribute<?, ?> treeNum1 = (Attribute<?, ?>) tree1.getChild("tree_num");
		assertEquals(new TextValue("1"), treeNum1.getValue());
		
		Attribute<?, ?> treeNum3 = (Attribute<?, ?>) tree3.getChild("tree_num");
		assertEquals(new TextValue("2"), treeNum3.getValue());
	}
	
	protected void record(EntityDefinition rootDef, NodeBuilder... builders) {
		record = NodeBuilder.record(survey, builders);
		updater.initializeRecord(record);
	}
	
	protected Entity entityByPath(String path) {
		return (Entity) record.findNodeByPath(path);
	}
	
	protected NodeChangeSet updateAttribute(String path, String value) {
		Attribute<?,?> attr = findAttribute(path);
		NodeChangeSet result = update(attr, value);
		return result;
	}

	protected NodeChangeSet update(Attribute<?, ?> attr, String value) {
		return updater.updateAttribute((Attribute<?, Value>) attr, new TextValue(value));
	}

	protected Attribute<?,?>  findAttribute(String path) {
		return (Attribute<?, ?>) record.findNodeByPath(path);
	}
	
	private CollectSurvey createTestSurvey() {
		CollectSurveyContext surveyContext = new CollectSurveyContext();
		CollectSurvey survey = (CollectSurvey) surveyContext.createSurvey();
		return survey;
	}
	
}
