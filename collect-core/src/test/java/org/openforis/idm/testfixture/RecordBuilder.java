package org.openforis.idm.testfixture;

import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Record;

public class RecordBuilder {
	private final NodeBuilder[] nodeBuilders;

	private RecordBuilder(NodeBuilder[] nodeBuilders) {
		this.nodeBuilders = nodeBuilders;
	}

	public static RecordBuilder record(NodeBuilder... nodeBuilders) {
		return new RecordBuilder(nodeBuilders);
	}

	public <R extends Record> R build(Survey survey) {
		return build(survey, (String) null, (String) null);
	}

	public <R extends Record> R build(Survey survey, String rootEntityName, String versionName) {
		return NodeBuilder.record(survey, rootEntityName, versionName, nodeBuilders);
	}
}
