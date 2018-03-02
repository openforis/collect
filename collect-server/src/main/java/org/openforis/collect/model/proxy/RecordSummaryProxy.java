/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.ProxyContext;
import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
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
		if ( summaries != null ) {
			for (CollectRecordSummary summary : summaries) {
				result.add(new RecordSummaryProxy(summary, context));
			}
		}
		return result;
	}
	
	@ExternalizedProperty
	public int getSurveyId() {
		return summary.getSurvey().getId();
	}
	
	@ExternalizedProperty
	public Step getStep() {
		return summary.getStep();
	}

	@ExternalizedProperty
	public int getStepNumber() {
		return summary.getStep().getStepNumber();
	}
	
	@ExternalizedProperty
	public State getState() {
		//return summary.getState();
		return null;
	}

	@ExternalizedProperty
	public Date getCreationDate() {
		return summary.getCreationDate();
	}

	@ExternalizedProperty
	public BasicUserProxy getCreatedBy() {
		User createdBy = summary.getCreatedBy();
		return createdBy == null ? null: new BasicUserProxy(createdBy);
	}

	@ExternalizedProperty
	public Date getModifiedDate() {
		return summary.getModifiedDate();
	}

	@ExternalizedProperty
	public Integer getId() {
		return summary.getId();
	}

	@ExternalizedProperty
	public BasicUserProxy getModifiedBy() {
		User modifiedBy = summary.getModifiedBy();
		return modifiedBy == null ? null: new BasicUserProxy(modifiedBy);
	}
	
	@ExternalizedProperty
	public ModelVersionProxy getVersion() {
		ModelVersion version = summary.getVersion();
		return version == null ? null: new ModelVersionProxy(version);
	}

	@ExternalizedProperty
	public List<String> getRootEntityKeys() {
		return summary.getRootEntityKeyValues();
	}

	@ExternalizedProperty
	public List<Integer> getEntityCounts() {
		return summary.getEntityCounts();
	}
	
	@ExternalizedProperty
	public boolean isEntryComplete() {
		if(summary.getStep() != null) {
			switch(summary.getStep()) {
			case ENTRY:
				return false;
			case CLEANSING:
			case ANALYSIS:
				return true;
			}
		}
		return false;
	}
	
	@ExternalizedProperty
	public boolean isCleansingComplete() {
		if(summary.getStep() != null) {
			switch(summary.getStep()) {
				case ANALYSIS:
					return true;
				default:
					return false;
			}
		}
		return false;
	}

	@ExternalizedProperty
	public Map<Step, StepSummary> getStepSummaries() {
		return summary.getStepSummaries();
	}
	
	@ExternalizedProperty
	public List<String> getSummaryValues() {
		return summary.getCurrentStepSummary().getSummaryValues();
	}
	
	@ExternalizedProperty
	public Integer getTotalErrors() {
		return summary.getCurrentStepSummary().getTotalErrors();
	}
	
	@ExternalizedProperty
	public Integer getErrors() {
		return summary.getCurrentStepSummary().getErrors();
	}

	@ExternalizedProperty
	public Integer getSkipped() {
		return summary.getCurrentStepSummary().getSkipped();
	}

	@ExternalizedProperty
	public Integer getMissing() {
		return summary.getCurrentStepSummary().getMissing();
	}

	@ExternalizedProperty
	public Integer getWarnings() {
		return summary.getCurrentStepSummary().getWarnings();
	}

	@ExternalizedProperty
	public Integer getMissingErrors() {
		return summary.getCurrentStepSummary().getMissingErrors();
	}

	@ExternalizedProperty
	public Integer getMissingWarnings() {
		return summary.getCurrentStepSummary().getMissingWarnings();
	}

	@ExternalizedProperty
	public UserProxy getOwner() {
		return Proxies.fromObject(summary.getOwner(), UserProxy.class);
	}
	
}
