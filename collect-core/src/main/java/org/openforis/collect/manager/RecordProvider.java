package org.openforis.collect.manager;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;

public interface RecordProvider {

	CollectRecord provide(CollectSurvey survey, Integer recordId, Step recordStep);

}
