package org.openforis.collect.designer.viewmodel;

import java.util.Collections;
import java.util.List;

import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.SamplingDesignItem;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class SamplingPointDataVM extends SurveyBaseVM {

	@WireVariable
	private SamplingDesignManager samplingDesignManager;
	
	@Init(superclass=false)
	public void init() {
		super.init();
	}
	
	public List<SamplingDesignItem> getSamplingPointItems() {
		Integer surveyId = getSurveyId();
		if (surveyId == null) {
			//TODO session expired
			return Collections.emptyList();
		}
		int offset = 0;
		int maxRecords = 30;
		return samplingDesignManager.loadBySurvey(surveyId, offset, maxRecords).getRecords();
	}
	
	public String getLevelCode(SamplingDesignItem item, int level) {
		return item.getLevelCodes().size() >= level
			? item.getLevelCode(level)
			: null;
	}
	
}
