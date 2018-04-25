/**
 * 
 */
package org.openforis.collect.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openforis.idm.testfixture.NodeBuilder.attribute;
import static org.openforis.idm.testfixture.NodeBuilder.entity;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.attributeDef;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.entityDef;

import org.junit.Test;
import org.openforis.collect.utils.Dates;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.TextAttribute;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.testfixture.NodeBuilder;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class RecordUpdaterTest extends AbstractRecordTest {

	@Override
	protected CollectSurvey createTestSurvey() {
		CollectSurveyContext surveyContext = new CollectSurveyContext();
		CollectSurvey survey = (CollectSurvey) surveyContext.createSurvey();
		addModelVersion(survey, "1.0", "2014-12-01");
		addModelVersion(survey, "2.0", "2014-12-02");
		addModelVersion(survey, "3.0", "2014-12-03");
		return survey;
	}

	private void addModelVersion(CollectSurvey survey, String name, String date) {
		ModelVersion version = survey.createModelVersion();
		version.setName(name);
		version.setDate(Dates.parseDate(date));
		survey.addVersion(version);
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
		
		Attribute<?, ?> treeCount = attributeByPath("/root/tree[1]/tree_count");
		assertEquals(new TextValue("1"), treeCount.getValue());
	}

	@Test
	public void testUpdateAttribute() {
		record(
			rootEntityDef(
				attributeDef("attribute")
			),
			attribute("attribute", "initial value")
		);
		
		Attribute<?,?> attr = attributeByPath("root/attribute[1]");
		
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
	public void testRequirenessUpdatedInNestedNodesOnRecordInitialization() {
		record(
			rootEntityDef(
				entityDef("plot",
					entityDef("tree",
						attributeDef("health"),
						attributeDef("start_time")
							.required("health = '1'")
					)
					.multiple()
				)
				.multiple()
			), 
			entity("plot", 
				entity("tree", 
					attribute("health", "1")),
				entity("tree")
			)
		);
		Entity tree = record.findNodeByPath("/root/plot[1]/tree[1]");
		assertTrue(tree.isRequired("start_time"));
	}

	@Test
	public void testMinCountValidationResultOnEntityWhenRequiredAttributeIsEmpty() {
		record(
			rootEntityDef(
				entityDef("time_study",
						attributeDef("start_time")
							.required()
				)
			),
			entity("time_study")
		);
		Entity timeStudy = entityByPath("/root/time_study");
		assertEquals(ValidationResultFlag.ERROR, timeStudy.getMinCountValidationResult("start_time"));
	}

	@Test
	public void testMinCountValidationInitializedOnNestedEntity() {
		record(
			rootEntityDef(
				entityDef("time_study",
						attributeDef("start_time")
							.required()
				).minCount("1")
			)
		);
		Entity timeStudy = entityByPath("/root/time_study");
		assertEquals(ValidationResultFlag.ERROR, timeStudy.getMinCountValidationResult("start_time"));
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
			),
			entity("time_study")
		);
		Entity rootEntity = record.getRootEntity();
		Attribute<?, ?> startTime = record.findNodeByPath("/root/time_study[1]/start_time");
		
		NodeChangeSet nodeChangeSet = update(startTime, "updated");
		
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
		Entity timeStudy2 = (Entity) rootEntity.getChild("time_study", 0);
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
	
	@Test
	public void testChildrenRelevanceTriggered() {
		record(
			rootEntityDef(
				attributeDef("tree_relevant"),
				entityDef("tree",
					attributeDef("tree_num")
				)
				.relevant("tree_relevant = 'yes'")
				.multiple()
			),
			entity("tree")
		);
		Attribute<?, ?> treeNum = attributeByPath("/root/tree[1]/tree_num");
		
		assertFalse(treeNum.isRelevant());

		update("/root/tree_relevant", "yes");
		
		assertTrue(treeNum.isRelevant());
	}
	
	@Test
	public void testConstantRelevanceEvaluated() {
		record(
			rootEntityDef(
				attributeDef("attr")
					.relevant("false()"),
				entityDef("tree",
					attributeDef("not_relevant")
						.relevant("false()")
				)
				.relevant("attr = 'yes'")
				.multiple()
			),
			entity("tree")
		);
		Entity tree = entityByPath("/root/tree[1]");
		Attribute<?, ?> attr = attributeByPath("/root/attr");
		Attribute<?, ?> notRelevantAttr = attributeByPath("/root/tree[1]/not_relevant");
		
		assertFalse(tree.isRelevant());
		assertFalse(attr.isRelevant());
		assertFalse(notRelevantAttr.isRelevant());

		update("/root/attr", "yes");
		
		assertTrue(tree.isRelevant());
		assertFalse(notRelevantAttr.isRelevant());
	}
	
	@Test
	public void testInitializeRelevanceDependencyInNestedNode() {
		record(
			rootEntityDef(
				entityDef("details",
					attributeDef("accessibility")
				),
				entityDef("tree",
					attributeDef("dbh")
						.relevant("parent()/details/accessibility = 'true'")
				).multiple()
			),
			entity("details",
				NodeBuilder.attribute("accessibility", "false")
			),
			entity("tree")
		);
		
		Node<?> dbh = record.findNodeByPath("/root/tree[1]/dbh");
		
		assertFalse(dbh.isRelevant());
		
		Attribute<?, ?> accessibility = (Attribute<?, ?>) record.findNodeByPath("/root/details/accessibility");
		update(accessibility, "true");
		
		assertTrue(dbh.isRelevant());
	}

	@Test
	public void testInitializeRelevanceDependencyWithEmptyAttribute() {
		record(
			rootEntityDef(
				entityDef("plot",
					attributeDef("accessibility"),
					entityDef("plot_details",
						attributeDef("plot_no")
					).relevant("accessibility = 'true'")
				).multiple()
			)
		);
		
		updater.addEntity(record.getRootEntity(), "plot");
		Node<?> plotDetails = record.findNodeByPath("/root/plot[1]/plot_details");
		assertFalse(plotDetails.isRelevant());
		
		Attribute<?, ?> accessibility = record.findNodeByPath("/root/plot[1]/accessibility");
		update(accessibility, "true");
		
		assertTrue(plotDetails.isRelevant());
	}
	
	@Test
	public void testRelevanceUpdatedFromNestedNode() {
		record(
			rootEntityDef(
				entityDef("tree",
					attributeDef("tree_no"),
					attributeDef("total_height")
						.calculated("tree_no * 2")
				),
				attributeDef("test")
					.relevant("tree[1]/total_height > 4")
			),
			entity("tree",
				attribute("tree_no", "1")
			),
			attribute("test")
		);
		
		Attribute<?, ?> test = record.findNodeByPath("/root/test");
		assertFalse(test.isRelevant());
		
		Attribute<?, ?> treeNo = record.findNodeByPath("/root/tree[1]/tree_no");
		update(treeNo, "3");
		
		assertTrue(test.isRelevant());
	}
	
	@Test
	public void testRelevanceUpdatedOnEmptyNodes() {
		record(
			rootEntityDef(
				attributeDef("accessibility"),
				entityDef("tree",
					attributeDef("tree_no")
				).relevant("accessibility = 'true'")
			),
			attribute("accessibility", "false"),
			entity("tree")
		);
		
		Entity tree = record.findNodeByPath("/root/tree[1]");
		
		assertFalse(tree.isRelevant());

		Attribute<?, ?> accessibility = record.findNodeByPath("/root/accessibility");
		update(accessibility, "true");
		
		assertTrue(tree.isRelevant());
	}

	@Test
	public void testRelevanceUpdatedFromNestedNodeWhenEntityIsAdded() {
		record(
			rootEntityDef(
				entityDef("tree",
					attributeDef("tree_pos")
						.calculated("idm:position()"),
					attributeDef("total_height")
						.calculated("tree_pos * 2")
				),
				attributeDef("test")
					.relevant("tree[2]/total_height > 3")
			)
		);
		
		Attribute<?, ?> test = record.findNodeByPath("/root/test");
		assertFalse(test.isRelevant());
		
		updater.addEntity(record.getRootEntity(), "tree");
		
		assertTrue(test.isRelevant());
	}
	
	@Test
	public void testRelevanceUpdatedInSiblingsWhenEntityIsAdded() {
		record(
			rootEntityDef(
				entityDef("land_feature")
					.multiple(),
				entityDef("land_feature_proportioning")
					.multiple()
					.relevant("count(land_feature) > 1")
			)
		);
		
		assertFalse(record.getRootEntity().isRelevant("land_feature_proportioning"));
		
		updater.addEntity(record.getRootEntity(), "land_feature");
		
		assertFalse(record.getRootEntity().isRelevant("land_feature_proportioning"));
		
		updater.addEntity(record.getRootEntity(), "land_feature");
		
		assertTrue(record.getRootEntity().isRelevant("land_feature_proportioning"));
	}
	
	@Test
	public void testRelevanceUpdatedInSiblingsWhenEntityIsRemoved() {
		record(
			rootEntityDef(
				entityDef("land_feature")
					.multiple(),
				entityDef("land_feature_proportioning")
					.multiple()
					.relevant("count(land_feature) > 1")
			)
		);
		
		updater.addEntity(record.getRootEntity(), "land_feature");
		updater.addEntity(record.getRootEntity(), "land_feature");
		
		Node<?> landFeature2 = record.findNodeByPath("/root/land_feature[2]");
		
		updater.deleteNode(landFeature2);
		
		assertFalse(record.getRootEntity().isRelevant("land_feature_proportioning"));
	}
	
	@Test
	public void testRelevanceNotUpdatedInAttributesNotInVersion() {
		record(
			rootEntityDef(
				attributeDef("trigger"),
				attributeDef("dependent")
					.relevant("trigger > 1"),
				attributeDef("deprecated")
					.relevant("trigger > 3")
					.deprecated("1.0"),
				attributeDef("since")
					.relevant("trigger > 3")
					.since("3.0")
			),
			"2.0"
		);
		Attribute<?, ?> trigger = record.findNodeByPath("/root/trigger");
		NodeChangeSet nodeChangeSet = update(trigger, "5");
		EntityChange rootEntityChange = (EntityChange) nodeChangeSet.getChange(record.getRootEntity());
		assertNull(rootEntityChange.getChildrenRelevance().get("deprecated"));
		assertNull(rootEntityChange.getChildrenRelevance().get("since"));
	}
	
	@Test
	public void testRelevanceUpdatedInDependentAttributes() {
		record(
			rootEntityDef(
				attributeDef("attribute1"),
				attributeDef("attribute2")
					.relevant("attribute1 = 1"),
				attributeDef("attribute3")
					.relevant("attribute2 = 2")
			),
			attribute("attribute1", "1"),
			attribute("attribute2", "2"),
			attribute("attribute3", "3")
		);
		Attribute<?, ?> attribute1 = record.findNodeByPath("/root/attribute1");
		
		updater.setClearNotRelevantAttributes(true);
		NodeChangeSet nodeChangeSet = update(attribute1, "2");
		
		//expected: attribute2 and attribute3 become not relevant
		EntityChange rootEntityChange = (EntityChange) nodeChangeSet.getChange(record.getRootEntity());
		Boolean attribute2Relevance = rootEntityChange.getChildrenRelevance().get("attribute2");
		assertNotNull(attribute2Relevance);
		assertFalse(attribute2Relevance);
		Boolean attribute3Relevance = rootEntityChange.getChildrenRelevance().get("attribute3");
		assertNotNull(attribute3Relevance);
		assertFalse(attribute3Relevance);
	}
	
	@Test
	public void testInitializeCalculatedAttributeInNestedNode() {
		record(
			rootEntityDef(
				entityDef("details",
					attributeDef("accessibility")
				),
				entityDef("tree",
					attributeDef("angle"),
					attributeDef("height")
						.calculated("number(angle) * 2"),
					attributeDef("double_height")
						.calculated("number(height) * 2", "parent()/details/accessibility = 'true'")
				).multiple()
			),
			entity("details",
				NodeBuilder.attribute("accessibility", "true")
			),
			entity("tree", NodeBuilder.attribute("angle", "10"))
		);
		
		TextAttribute doubleHeight = record.findNodeByPath("/root/tree[1]/double_height");
		
		assertEquals("40.0", ((TextValue) doubleHeight.getValue()).getValue());
	}
	
	@Test
	public void testCardinalityRevalidatedOnAttributeUpdate() {
		record(
			rootEntityDef(
				attributeDef("min_time_study"),
				entityDef("time_study",
					attributeDef("start_time")
				)
				.multiple()
				.minCount("min_time_study")
			),
			attribute("min_time_study", "2"),
			entity("time_study", 
				attribute("start_time", "2011")
			),
			entity("time_study", 
				attribute("start_time", "2012")
			)
		);
		Entity rootEntity = record.getRootEntity();
		assertEquals(ValidationResultFlag.OK, rootEntity.getMinCountValidationResult("time_study"));

		Attribute<?, ?> minTimeStudy = record.findNodeByPath("/root/min_time_study");

		NodeChangeSet nodeChangeSet = update(minTimeStudy, "3");
		assertNotNull(nodeChangeSet.getChange(rootEntity));
		assertEquals(ValidationResultFlag.ERROR, rootEntity.getMinCountValidationResult("time_study"));
	}
	
	@Test
	public void testCardinalityRevalidatedOnRequiredAttributeUpdate() {
		record(
			rootEntityDef(
				attributeDef("source"),
				attributeDef("dependent")
					.required("source = 1")
			),
			attribute("source", "2"),
			attribute("dependent", null)
		);
		Entity rootEntity = record.getRootEntity();
		assertEquals(ValidationResultFlag.OK, rootEntity.getMinCountValidationResult("dependent"));

		Attribute<?, ?> source = record.findNodeByPath("/root/source");

		NodeChangeSet nodeChangeSet = update(source, "1");
		EntityChange rootEntityChange = (EntityChange) nodeChangeSet.getChange(rootEntity);
		assertNotNull(rootEntityChange);
		ValidationResultFlag dependentValidationResult = rootEntityChange.getChildrenMinCountValidation().get("dependent");
		assertEquals(ValidationResultFlag.ERROR, dependentValidationResult);
		assertEquals(ValidationResultFlag.ERROR, rootEntity.getMinCountValidationResult("dependent"));
	}
	
	@Test
	public void testCardinalityRevalidatedWhenBecomesRelevant() {
		record(
			rootEntityDef(
				attributeDef("source"),
				attributeDef("dependent")
					.multiple()
					.relevant("source = '1'")
					.minCount("1")
			),
			attribute("source", "2"),
			attribute("dependent", null)
		);
		Entity rootEntity = record.getRootEntity();
		assertEquals(ValidationResultFlag.OK, rootEntity.getMinCountValidationResult("dependent"));

		Attribute<?, ?> source = record.findNodeByPath("/root/source");

		NodeChangeSet nodeChangeSet = update(source, "1");
		EntityChange rootEntityChange = (EntityChange) nodeChangeSet.getChange(rootEntity);
		assertNotNull(rootEntityChange);
		ValidationResultFlag dependentValidationResult = rootEntityChange.getChildrenMinCountValidation().get("dependent");
		assertEquals(ValidationResultFlag.ERROR, dependentValidationResult);
		assertEquals(ValidationResultFlag.ERROR, rootEntity.getMinCountValidationResult("dependent"));
	}
	
	@Test
	public void testCardinalityRevalidatedOnDelete() {
		record(
			rootEntityDef(
				attributeDef("max_time_study"),
				entityDef("time_study",
					attributeDef("start_time")
				)
				.multiple()
				.maxCount("max_time_study")
			),
			attribute("max_time_study", "2"),
			entity("time_study", 
				attribute("start_time", "2011")
			),
			entity("time_study", 
				attribute("start_time", "2012")
			),
			entity("time_study", 
				attribute("start_time", "2013")
			)
		);
		Entity rootEntity = record.getRootEntity();
		
		assertEquals(ValidationResultFlag.ERROR, rootEntity.getMaxCountValidationResult("time_study"));
		
//		EntityDefinition timeStudyDef = (EntityDefinition) survey.getSchema().getDefinitionByPath("/root/time_study");
//		Integer timeStudyMaxCount = rootEntityChange.getMaxCountByChildDefinitionId().get(timeStudyDef);
		
		NodeChangeSet changeSet = updater.deleteNode(record.findNodeByPath("/root/time_study[1]"));
		EntityChange rootEntityChange = (EntityChange) changeSet.getChange(rootEntity);
		assertNotNull(rootEntityChange);
		ValidationResultFlag maxTimeStudyCountValidation = rootEntityChange.getChildrenMaxCountValidation().get("time_study");
		assertEquals(ValidationResultFlag.OK, maxTimeStudyCountValidation);
		assertEquals(ValidationResultFlag.OK, rootEntity.getMaxCountValidationResult("time_study"));
	}
	
}
