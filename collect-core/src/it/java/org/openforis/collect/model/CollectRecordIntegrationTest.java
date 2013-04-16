/**
 * 
 */
package org.openforis.collect.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.NodeUpdateResponse.AddNodeResponse;
import org.openforis.collect.model.NodeUpdateResponse.DeleteNodeResponse;
import org.openforis.collect.model.NodeUpdateResponse.EntityUpdateResponse;
import org.openforis.collect.model.RecordUpdateRequest.AddEntityRequest;
import org.openforis.collect.model.RecordUpdateRequest.DeleteNodeRequest;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.model.Code;
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
	
	@Test
	public void testAddPlot() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		RecordUpdateRequestSet requestSet = new RecordUpdateRequestSet();
		List<RecordUpdateRequest> requests = new ArrayList<RecordUpdateRequest>();
		AddEntityRequest r = new RecordUpdateRequest.AddEntityRequest();
		Entity cluster = record.getRootEntity();
		r.setParentEntityId(cluster.getInternalId());
		r.setNodeName("plot");
		requests.add(r);
		requestSet.setRequests(requests);
		RecordUpdateResponseSet responseSet = record.update(requestSet);
		assertNotNull(responseSet);
		Collection<NodeUpdateResponse<?>> responses = responseSet.getResponses();
		assertNotNull(responses);
		assertEquals(2, responses.size());
		Iterator<NodeUpdateResponse<?>> respIt = responses.iterator();
		{
			NodeUpdateResponse<?> clusterUpdateResponse = respIt.next();
			assertTrue(clusterUpdateResponse instanceof EntityUpdateResponse);
			EntityUpdateResponse clusterEntityResp = (EntityUpdateResponse) clusterUpdateResponse;
			Map<String, ValidationResultFlag> childrenMinCountValid = clusterEntityResp.getChildrenMinCountValidation();
			ValidationResultFlag plotMinCountValid = childrenMinCountValid.get("plot");
			assertTrue(plotMinCountValid == ValidationResultFlag.OK);
			Map<String, ValidationResultFlag> childrenMaxCountValid = clusterEntityResp.getChildrenMaxCountValidation();
			ValidationResultFlag plotMaxCountValid = childrenMaxCountValid.get("plot");
			assertTrue(plotMaxCountValid == ValidationResultFlag.OK);
		}
		{
			NodeUpdateResponse<?> plotUpdateResponse = respIt.next();
			assertTrue(plotUpdateResponse instanceof AddNodeResponse);
			assertTrue(plotUpdateResponse instanceof EntityUpdateResponse);
			EntityUpdateResponse plotUpdateResp = (EntityUpdateResponse) plotUpdateResponse;
			Entity plot = plotUpdateResp.getNode();
			assertNotNull(plot);
			assertEquals("plot", plot.getName());
		}
	}
	
	@Test
	public void testAddTimeStudy() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		RecordUpdateRequestSet requestSet = new RecordUpdateRequestSet();
		List<RecordUpdateRequest> requests = new ArrayList<RecordUpdateRequest>();
		AddEntityRequest r = new RecordUpdateRequest.AddEntityRequest();
		Entity cluster = record.getRootEntity();
		r.setParentEntityId(cluster.getInternalId());
		r.setNodeName("time_study");
		requests.add(r);
		requestSet.setRequests(requests);
		RecordUpdateResponseSet responseSet = record.update(requestSet);
		assertNotNull(responseSet);
		Collection<NodeUpdateResponse<?>> responses = responseSet.getResponses();
		assertNotNull(responses);
		assertEquals(2, responses.size());
		Iterator<NodeUpdateResponse<?>> respIt = responses.iterator();
		{
			NodeUpdateResponse<?> clusterUpdateResponse = respIt.next();
			assertTrue(clusterUpdateResponse instanceof EntityUpdateResponse);
			EntityUpdateResponse clusterUpdateResp = (EntityUpdateResponse) clusterUpdateResponse;
			Map<String, ValidationResultFlag> childrenMinCountValid = clusterUpdateResp.getChildrenMinCountValidation();
			ValidationResultFlag plotMinCountValid = childrenMinCountValid.get("time_study");
			assertTrue(plotMinCountValid == ValidationResultFlag.OK);
			Map<String, ValidationResultFlag> childrenMaxCountValid = clusterUpdateResp.getChildrenMaxCountValidation();
			ValidationResultFlag plotMaxCountValid = childrenMaxCountValid.get("time_study");
			assertTrue(plotMaxCountValid == ValidationResultFlag.ERROR);
		}
		{
			NodeUpdateResponse<?> timeStudyUpdateResponse = respIt.next();
			assertTrue(timeStudyUpdateResponse instanceof AddNodeResponse);
			assertTrue(timeStudyUpdateResponse instanceof EntityUpdateResponse);
			EntityUpdateResponse timeStudyUpdateResp = (EntityUpdateResponse) timeStudyUpdateResponse;
			Entity timeStudy = timeStudyUpdateResp.getNode();
			assertNotNull(timeStudy);
			assertEquals("time_study", timeStudy.getName());
		}
	}
	
	@Test
	public void testRemoveTimeStudy() throws Exception {
		CollectSurvey survey = loadSurvey();
		CollectRecord record = createTestRecord(survey);
		RecordUpdateRequestSet requestSet = new RecordUpdateRequestSet();
		List<RecordUpdateRequest> requests = new ArrayList<RecordUpdateRequest>();
		Entity cluster = record.getRootEntity();
		DeleteNodeRequest r1 = new RecordUpdateRequest.DeleteNodeRequest();
		Entity timeStudy1 = (Entity) cluster.get("time_study", 0);
		r1.setNode(timeStudy1);
		requests.add(r1);
		DeleteNodeRequest r2 = new RecordUpdateRequest.DeleteNodeRequest();
		Entity timeStudy2 = (Entity) cluster.get("time_study", 1);
		r2.setNode(timeStudy2);
		requests.add(r2);
		requestSet.setRequests(requests);
		RecordUpdateResponseSet responseSet = record.update(requestSet);
		assertNotNull(responseSet);
		Collection<NodeUpdateResponse<?>> responses = responseSet.getResponses();
		assertNotNull(responses);
		assertEquals(3, responses.size());
		{
			NodeUpdateResponse<?> clusterResponse = responseSet.getResponse(cluster);
			assertTrue(clusterResponse instanceof EntityUpdateResponse);
			EntityUpdateResponse clusterUpdateResp = (EntityUpdateResponse) clusterResponse;
			Map<String, ValidationResultFlag> childrenMinCountValid = clusterUpdateResp.getChildrenMinCountValidation();
			ValidationResultFlag timeStudyMinCountValid = childrenMinCountValid.get("time_study");
			assertTrue(timeStudyMinCountValid == ValidationResultFlag.ERROR);
			Map<String, ValidationResultFlag> childrenMaxCountValid = clusterUpdateResp.getChildrenMaxCountValidation();
			ValidationResultFlag timeStudyMaxCountValid = childrenMaxCountValid.get("time_study");
			assertTrue(timeStudyMaxCountValid == ValidationResultFlag.OK);
		}
		{
			NodeUpdateResponse<?> timeStudyDeleteResponse = responseSet.getResponse(timeStudy1);
			assertTrue(timeStudyDeleteResponse instanceof DeleteNodeResponse);
			DeleteNodeResponse timeStudyDeleteResp = (DeleteNodeResponse) timeStudyDeleteResponse;
			Node<?> deletedNode = timeStudyDeleteResp.getNode();
			assertEquals(timeStudy1.getInternalId(), deletedNode.getInternalId());
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

}
