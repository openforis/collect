/**
 * 
 */
package org.openforis.collect.api;

import static org.openforis.idm.testfixture.NodeDefinitionBuilder.*;
import static org.openforis.idm.testfixture.RecordBuilder.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.api.command.SynchronousCommandQueue;
import org.openforis.collect.api.command.UpdateAttributeValueCommand;
import org.openforis.collect.api.event.Event;
import org.openforis.collect.api.event.EventHandler;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.model.User;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.testfixture.TestFixture;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class ApiTest implements EventHandler {
	
	private SynchronousCommandQueue queue;
	private User user;
	private TestRecordProvider recordProvider;
	private Record record;
	private List<Event> recievedEvents = new ArrayList<Event>();

	@Before
	public void setup() {
		recordProvider = new TestRecordProvider();
		queue = new SynchronousCommandQueue(recordProvider, this);
		user = new org.openforis.collect.model.User("test");
		record = recordProvider.provideRecord(1);
	}
	
	@Test
	public void test() {
		int recordId = record.getId();
		Attribute<?, ?> attr = (Attribute<?, ?>) record.findNodeByPath("/root/attr");
		int attributeId = attr.getInternalId();
		UpdateAttributeValueCommand command = new UpdateAttributeValueCommand(recordId, user, attributeId, new TextValue("text"));
		
		submit(command);
		
		System.out.println(recievedEvents);
	}

	@Override
	public void handle(List<Event> events) {
		recievedEvents.addAll(events);
	}
	
	private void submit(UpdateAttributeValueCommand command) {
		queue.submit(command);
	}

	static class TestRecordProvider implements RecordProvider {

		private TestFixture fixture;

		public TestRecordProvider() {
			fixture = TestFixture.survey(
						rootEntityDef("root",
							attributeDef("attr")
						), 
						record());
			RecordUpdater recordUpdater = new RecordUpdater();
			
			List<Record> records = fixture.records;
			for (int i = 0; i < records.size(); i++) {
				Record record = records.get(i);
				record.setId(i + 1);
				recordUpdater.initializeRecord(record);
			}
		}
		
		@Override
		public Record provideRecord(int recordId) {
			return fixture.records.get(0);
		}
		
	}
	
}
