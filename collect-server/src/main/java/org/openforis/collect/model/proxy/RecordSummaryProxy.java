/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openforis.collect.Proxy;
import org.openforis.collect.ProxyContext;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectRecordSummary.StepSummary;
import org.openforis.collect.model.User;
import org.openforis.collect.utils.Proxies;
import org.openforis.idm.metamodel.ModelVersion;

/**
 * @author S. Ricci
 * 
 */
public class RecordSummaryProxy implements Proxy {

	private transient CollectRecordSummary summary;
	private transient ProxyContext context;

	public RecordSummaryProxy(CollectRecordSummary summary, ProxyContext context) {
		this.summary = summary;
		this.context = context;
	}

	public static List<RecordSummaryProxy> fromList(List<CollectRecordSummary> summaries, ProxyContext context) {
		List<RecordSummaryProxy> result = new ArrayList<RecordSummaryProxy>();
		if (summaries != null) {
			for (CollectRecordSummary summary : summaries) {
				result.add(new RecordSummaryProxy(summary, context));
			}
		}
		return result;
	}

	public int getSurveyId() {
		return summary.getSurvey().getId();
	}

	public Step getStep() {
		return summary.getStep();
	}

	public int getStepNumber() {
		return summary.getStep().getStepNumber();
	}

	public State getState() {
		// return summary.getState();
		return null;
	}

	public Date getCreationDate() {
		return summary.getCreationDate();
	}

	public BasicUserProxy getCreatedBy() {
		User createdBy = summary.getCreatedBy();
		return createdBy == null ? null : new BasicUserProxy(createdBy);
	}

	public Date getModifiedDate() {
		return summary.getModifiedDate();
	}

	public Integer getId() {
		return summary.getId();
	}

	public BasicUserProxy getModifiedBy() {
		User modifiedBy = summary.getModifiedBy();
		return modifiedBy == null ? null : new BasicUserProxy(modifiedBy);
	}

	public Integer getVersionId() {
		ModelVersion version = getVersion();
		return version == null ? null : version.getId();
	}

	public List<String> getRootEntityKeys() {
		return summary.getRootEntityKeyValues();
	}

	public List<Integer> getEntityCounts() {
		return summary.getEntityCounts();
	}

	public boolean isEntryComplete() {
		if (summary.getStep() != null) {
			switch (summary.getStep()) {
			case ENTRY:
				return false;
			case CLEANSING:
			case ANALYSIS:
				return true;
			}
		}
		return false;
	}

	public boolean isCleansingComplete() {
		if (summary.getStep() != null) {
			switch (summary.getStep()) {
			case ANALYSIS:
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	public Map<Step, StepSummary> getStepSummaries() {
		return summary.getStepSummaries();
	}

	public List<String> getSummaryValues() {
		return summary.getCurrentStepSummary().getSummaryValues();
	}

	public Integer getTotalErrors() {
		return summary.getCurrentStepSummary().getTotalErrors();
	}

	public Integer getErrors() {
		return summary.getCurrentStepSummary().getErrors();
	}

	public Integer getSkipped() {
		return summary.getCurrentStepSummary().getSkipped();
	}

	public Integer getMissing() {
		return summary.getCurrentStepSummary().getMissing();
	}

	public Integer getWarnings() {
		return summary.getCurrentStepSummary().getWarnings();
	}

	public Integer getMissingErrors() {
		return summary.getCurrentStepSummary().getMissingErrors();
	}

	public Integer getMissingWarnings() {
		return summary.getCurrentStepSummary().getMissingWarnings();
	}

	public UserProxy getOwner() {
		return Proxies.fromObject(summary.getOwner(), UserProxy.class);
	}

	public String getLockedBy() {
		return summary.getLockedBy();
	}

	private ModelVersion getVersion() {
		return summary.getVersion();
	}

}
