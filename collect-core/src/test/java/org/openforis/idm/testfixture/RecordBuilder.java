package org.openforis.idm.testfixture;

import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Record;

public class RecordBuilder {
	private final NodeBuilder[] nodeBuilders;

	private  RecordBuilder(NodeBuilder[] nodeBuilders) {
		this.nodeBuilders = nodeBuilders;
	}

	public static RecordBuilder record(NodeBuilder... nodeBuilders) {
		return new RecordBuilder(nodeBuilders);
	}

	public Record build(Survey survey) {
		return NodeBuilder.record(survey, nodeBuilders);
	}
}
