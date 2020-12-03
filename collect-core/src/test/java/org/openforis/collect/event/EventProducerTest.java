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
import java.util.Locale;

import org.junit.Test;
import org.openforis.collect.event.EventProducer.EventProducerContext;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.model.AbstractRecordTest;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.model.Entity;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class EventProducerTest extends AbstractRecordTest {

	@Override
	public void init() {
		super.init();
	}

	@Test
	public void testEventTypes() {
		record(rootEntityDef(entityDef("tree", attributeDef("tree_count")).multiple()));
		assertEventTypes(updater.addEntity(record.getRootEntity(), "tree"), EntityCreatedEvent.class,
				AttributeCreatedEvent.class, EntityCreationCompletedEvent.class, TextAttributeUpdatedEvent.class);
	}

	@Test
	public void testEntityCollectionAddedEvent() {
		record(rootEntityDef(entityDef("plot", entityDef("tree", attributeDef("tree_count")).multiple()).multiple()));
		assertEventTypes(updater.addEntity(record.getRootEntity(), "plot"), EntityCreatedEvent.class,
				EntityCollectionCreatedEvent.class, EntityCreationCompletedEvent.class);

		Entity plot = entityByPath("/root/plot[1]");
		assertEventTypes(updater.addEntity(plot, "tree"), EntityCreatedEvent.class, AttributeCreatedEvent.class,
				EntityCreationCompletedEvent.class, TextAttributeUpdatedEvent.class);

	}

	private void assertEventTypes(NodeChangeSet changeSet, Class<?>... expectedEventTypes) {
		final List<RecordEvent> events = new ArrayList<RecordEvent>();
		EventProducer eventProducer = createEventProducer(events);
		eventProducer.produceFor(changeSet);

		List<Class<?>> actualEventTypes = new ArrayList<Class<?>>(events.size());
		for (RecordEvent event : events) {
			actualEventTypes.add(event.getClass());
		}
		assertEquals(Arrays.asList(expectedEventTypes), actualEventTypes);
	}

	private EventProducer createEventProducer(final List<RecordEvent> events) {
		MessageSource messageSource = new MessageSource() {
			@Override
			public String getMessage(Locale locale, String code, Object... args) {
				return null;
			}
		};
		EventProducerContext context = new EventProducerContext(messageSource, "user_name");
		EventProducer eventProducer = new EventProducer(context, new EventListenerToList(events));
		return eventProducer;
	}

}
