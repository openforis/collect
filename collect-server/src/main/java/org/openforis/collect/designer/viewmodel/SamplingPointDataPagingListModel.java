package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.model.AbstractPagingListModel;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;

public class SamplingPointDataPagingListModel extends AbstractPagingListModel<SamplingDesignItem> {

	private static final long serialVersionUID = 8121965590327085378L;

	private SamplingDesignManager samplingDesignManager;
	private int surveyId;

	public SamplingPointDataPagingListModel(SamplingDesignManager samplingDesignManager, int surveyId,
			int startPageNumber, int pageSize) {
		super(startPageNumber, pageSize);
		this.samplingDesignManager = samplingDesignManager;
		this.surveyId = surveyId;
		
		this.initItems();
	}

	@Override
	protected List<SamplingDesignItem> getPageData(int itemStartNumber, int pageSize) {
		SamplingDesignSummaries summaries = samplingDesignManager.loadBySurvey(surveyId, itemStartNumber, pageSize);
		return summaries.getRecords();
	}

	@Override
	public int getTotalSize() {
		return samplingDesignManager.countBySurvey(surveyId);
	}

}
