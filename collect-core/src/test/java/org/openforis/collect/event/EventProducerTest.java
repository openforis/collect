/**
 * 
 */
package org.openforis.collect.event;

import static org.junit.Assert.assertEquals;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.attributeDef;
import static org.openforis.idm.testfixture.NodeDefinitionBuilder.entityDef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openforis.collect.model.AbstractRecordTest;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.model.Entity;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class EventProducerTest extends AbstractRecordTest {

	private EventProducer eventProducer;
	
	@Override
	public void init() {
		super.init();
		eventProducer = new EventProducer();
	}
	
	@Test
	public void testEventTypes() {
		record(
			rootEntityDef(
				entityDef("tree",
					attributeDef("tree_count")
				).multiple()
			)
		);
		assertEventTypes(updater.addEntity(record.getRootEntity(), "tree"),
				EntityCreatedEvent.class, TextAttributeUpdatedEvent.class);
	}
	
	@Test
	public void testEntityCollectionAddedEvent() {
		record(
			rootEntityDef(
				entityDef("plot",
					entityDef("tree",
							attributeDef("tree_count")
					).multiple()
				).multiple()
			)
		);
		assertEventTypes(updater.addEntity(record.getRootEntity(), "plot"),
				EntityCreatedEvent.class, EntityCollectionCreatedEvent.class);
		
		Entity plot = entityByPath("/root/plot[1]");
		assertEventTypes(updater.addEntity(plot, "tree"),
				EntityCreatedEvent.class, TextAttributeUpdatedEvent.class);
		
	}
	
	private void assertEventTypes(NodeChangeSet changeSet, Class<?>... expectedEventTypes) {
		List<RecordEvent> events = eventProducer.produceFor(changeSet, "user_name");
		List<Class<?>> actualEventTypes = new ArrayList<Class<?>>(events.size());
		for (RecordEvent event : events) {
			actualEventTypes.add(event.getClass());
		}
		assertEquals(Arrays.asList(expectedEventTypes), actualEventTypes);
	}

}
