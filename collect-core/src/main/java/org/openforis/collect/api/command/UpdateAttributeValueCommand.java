/**
 * 
 */
package org.openforis.collect.api.command;

import java.util.Arrays;
import java.util.List;

import org.openforis.collect.api.RecordProvider;
import org.openforis.collect.api.User;
import org.openforis.collect.api.event.Event;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.Value;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class UpdateAttributeValueCommand extends Command {

	public final Value value;
	public final int attributeId;

	public UpdateAttributeValueCommand(int recordId, User user, int attributeId, Value value) {
		super(recordId, user);
		this.attributeId = attributeId;
		this.value = value;
	}

	static class Handler implements CommandHandler<UpdateAttributeValueCommand> {

		private final RecordProvider recordProvider;
		private final RecordUpdater recordUpdater;
		
		public Handler(RecordProvider recordProvider, RecordUpdater recordUpdater) {
			this.recordProvider = recordProvider;
			this.recordUpdater = recordUpdater;
		}

		@Override
		public List<Event> handle(UpdateAttributeValueCommand command) {
			Record record = recordProvider.provideRecord(command.recordId);
			@SuppressWarnings("unchecked")
			Attribute<?, Value> attribute = (Attribute<?, Value>) record.getNodeByInternalId(command.attributeId);
			NodeChangeSet changeSet = recordUpdater.updateAttribute(attribute, command.value);
			Event event = new Event(command) {
			};
			return Arrays.asList(event);
		}
		
	}
	
}
