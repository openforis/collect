package org.openforis.collect.manager;

import static junit.framework.Assert.assertEquals;
import static org.openforis.idm.testfixture.NodeBuilder.attribute;
import static org.openforis.idm.testfixture.NodeBuilder.entity;
import static org.openforis.idm.testfixture.RecordBuilder.record;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.MissingRecordKeyException;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.springframework.beans.factory.annotation.Autowired;

public class RecordManagerIntegrationTest extends CollectIntegrationTest {
	
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private RecordDao recordDao;
	@Autowired
	private UserManager userManager;
	
	private CollectSurvey survey;

	@Before
	public void init() throws SurveyImportException, IdmlParseException, SurveyValidationException { 
		this.survey = importModel();
	}
	
	@Test
	public void testNewRecordInsert() {
		CollectRecord record = (CollectRecord) record(
			attribute("id", "10_117"),
			attribute("region", "002"),
			attribute("district", "003")
		).build(survey, "cluster", "2.0");
		
		new RecordUpdater().initializeNewRecord(record);
		
		User user = userManager.loadAdminUser();
		record.setCreatedBy(user);
		record.setCreationDate(new Date());
		record.setModifiedBy(user);
		record.setModifiedDate(new Date());
		recordManager.save(record);
		
		CollectRecord reloaded = recordManager.load(survey, record.getId());
		
		assertEquals(record, reloaded);
	}
	
	@Test
	public void testPublish() throws MissingRecordKeyException, RecordPromoteException {
		CollectRecord record = (CollectRecord) record(
			attribute("id", "10_117"),
			attribute("region", "002"),
			attribute("district", "003")
		).build(survey, "cluster", "2.0");
		new RecordUpdater().initializeNewRecord(record);
		
		User user = userManager.loadAdminUser();
		
		record.setCreatedBy(user);
		record.setModifiedBy(user);
		
		recordManager.save(record);
		
		recordManager.promote(record, user, true);
		
		CollectRecord promotedRecord = recordManager.load(survey, record.getId());
		assertEquals(record, promotedRecord);
		
		List<CollectRecordSummary> fullSummaries = recordDao.loadFullSummaries(new RecordFilter(survey), null);
		assertEquals(1, fullSummaries.size());
		
		CollectRecordSummary recordSummary = fullSummaries.get(0);
		assertEquals(2, recordSummary.getStepSummaries().size());
	}
	
	@Test
	public void testUpdateCurrentStep() throws MissingRecordKeyException, RecordPromoteException {
		CollectRecord record = (CollectRecord) record(
			attribute("id", "10_117"),
			attribute("region", "002"),
			attribute("district", "003")
		).build(survey, "cluster", "2.0");
		RecordUpdater recordUpdater = new RecordUpdater();
		recordUpdater.initializeNewRecord(record);
		
		User user = userManager.loadAdminUser();
		
		record.setCreatedBy(user);
		record.setModifiedBy(user);
		
		recordManager.save(record);
		
		recordManager.promote(record, user, true);
		
		CollectRecord cleansingRecord = recordManager.load(survey, record.getId());
		Attribute<?, Code> district = cleansingRecord.findNodeByPath("/cluster/district");
		Code newDistrictValue = new Code("001");
		recordUpdater.updateAttribute(district, newDistrictValue);
		
		recordManager.save(cleansingRecord);
		
		CollectRecord reloadedCleansingRecord = recordManager.load(survey, record.getId());
		Attribute<?, Code> reloadedDistrict = reloadedCleansingRecord.findNodeByPath("/cluster/district");
		
		assertEquals(newDistrictValue, reloadedDistrict.getValue());
	}
	
	@Test
	public void testUpdateCurrentStepDoesntAffectPreviousStep() throws MissingRecordKeyException, RecordPromoteException {
		CollectRecord record = (CollectRecord) record(
			attribute("id", "10_117"),
			attribute("region", "002"),
			attribute("district", "003")
		).build(survey, "cluster", "2.0");
		RecordUpdater recordUpdater = new RecordUpdater();
		recordUpdater.initializeNewRecord(record);
		
		User user = userManager.loadAdminUser();
		
		record.setCreatedBy(user);
		record.setModifiedBy(user);
		
		recordManager.save(record);
		
		recordManager.promote(record, user, true);
		
		CollectRecord cleansingRecord = recordManager.load(survey, record.getId());
		Attribute<?, Code> district = cleansingRecord.findNodeByPath("/cluster/district");
		Code newDistrictValue = new Code("001");
		recordUpdater.updateAttribute(district, newDistrictValue);
		
		recordManager.save(cleansingRecord);
		
		CollectRecord entryRecord = recordManager.load(survey, record.getId(), Step.ENTRY);
		assertEquals(2, (int) entryRecord.getWorkflowSequenceNumber());
		assertEquals(1, (int) entryRecord.getDataWorkflowSequenceNumber());
		
		Attribute<?, Code> entryDistrict = entryRecord.findNodeByPath("/cluster/district");
		assertEquals(new Code("003"), entryDistrict.getValue());
	}
	
	@Test
	public void testRecordSummaryUpdated() {
		CollectRecord record = (CollectRecord) record(
			attribute("id", "10_114"),
			attribute("region", "001"),
			attribute("district", "002"),
			entity("plot",
				attribute("no", "1"),
				entity("tree",
					attribute("tree_no", "1"),
					attribute("dbh", "10")
				),
				entity("tree",
					attribute("tree_no", "2"),
					attribute("dbh", "20")
				)
			)
		).build(survey, "cluster", "2.0");
		
		User user = userManager.loadAdminUser();
		
		record.setCreatedBy(user);
		record.setModifiedBy(user);
		
		recordManager.save(record);
		
		CollectRecord reloadedRecord = recordManager.load(survey, record.getId());
		
		String regionQualifier = reloadedRecord.getQualifierValues().get(0);
		assertEquals("001", regionQualifier);
		
		String districtQualifier = reloadedRecord.getQualifierValues().get(1);
		assertEquals("002", districtQualifier);
		
		int treesCount = reloadedRecord.getEntityCounts().get(1);
		assertEquals(2, treesCount);
	}
	
	@Test
	public void testRecordSummaryUpdatedInCleansedRecord() throws MissingRecordKeyException, RecordPromoteException {
		CollectRecord record = (CollectRecord) record(
				attribute("id", "10_114"),
				attribute("region", "001"),
				attribute("district", "002"),
				entity("plot",
					attribute("no", "1"),
					entity("tree",
						attribute("tree_no", "1"),
						attribute("dbh", "10")
					),
					entity("tree",
						attribute("tree_no", "2"),
						attribute("dbh", "20")
					)
				)
			).build(survey, "cluster", "2.0");
			
		RecordUpdater recordUpdater = new RecordUpdater();
		User user = userManager.loadAdminUser();
		
		recordManager.promote(record, user, true);
		
		CollectRecord cleansingRecord = recordManager.load(survey, record.getId());
		Entity tree = cleansingRecord.findNodeByPath("/cluster/plot[1]/tree[2]");
		recordUpdater.deleteNode(tree);
		
		recordManager.save(cleansingRecord);
		CollectRecord reloadedCleansingRecord = recordManager.load(survey, record.getId());
		int cleansingTreesCount = reloadedCleansingRecord.getEntityCounts().get(1);
		assertEquals(1, cleansingTreesCount);
		
		
		CollectRecord reloadedEntryRecord = recordManager.load(survey, record.getId(), Step.ENTRY);
		int entryTreesCount = reloadedEntryRecord.getEntityCounts().get(1);
		assertEquals(2, entryTreesCount);
		
		List<CollectRecordSummary> fullSummaries = recordManager.loadFullSummaries(new RecordFilter(survey));
		CollectRecordSummary recordSummary = fullSummaries.get(0);
		int entryTreesCountInSummary = recordSummary.getSummaryByStep(Step.ENTRY).getEntityCounts().get(1);
		assertEquals(2, entryTreesCountInSummary);
		
		int cleansingTreesCountInSummary = recordSummary.getSummaryByStep(Step.CLEANSING).getEntityCounts().get(1);
		assertEquals(1, cleansingTreesCountInSummary);
	}
}
