package org.openforis.collect.manager;

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
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.TaxonOccurrence;
import org.springframework.beans.factory.annotation.Autowired;

import junit.framework.Assert;

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
	
//	@Test
	public void testNewRecordInsert() {
		CollectRecord record = (CollectRecord) record(
			attribute("id", "10_117"),
			attribute("region", "002"),
			attribute("district", "003"),
			entity("plot",
				attribute("no", "1"),
				entity("tree",
					attribute("tree_no", "1"),
					attribute("species", new TaxonOccurrence("UNL", "Unlisted species")),
					attribute("dbh", "20")
				),
				entity("tree",
					attribute("tree_no", "2"),
					attribute("species", new TaxonOccurrence("ACA", "Acacia Sp.")),
					attribute("dbh", "30")
				)
			)
		).build(survey, "cluster", "2.0");
		
		User user = userManager.loadAdminUser();
		record.setCreatedBy(user);
		record.setCreationDate(new Date());
		record.setModifiedBy(user);
		record.setModifiedDate(new Date());
		recordManager.save(record);
		
		List<CollectRecordSummary> fullSummaries = recordDao.loadFullSummaries(new RecordFilter(survey), null);
		
	}
}
