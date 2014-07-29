package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignSummariesProxy implements Proxy {

	private transient SamplingDesignSummaries summaries;
	private transient CollectSurvey survey;
	
	public SamplingDesignSummariesProxy(CollectSurvey survey, SamplingDesignSummaries summaries) {
		super();
		this.survey = survey;
		this.summaries = summaries;
	}

	@ExternalizedProperty
	public List<SamplingDesignItemProxy> getRecords() {
		return SamplingDesignItemProxy.fromList(summaries.getRecords());
	}

	@ExternalizedProperty
	public int getTotalCount() {
		return summaries.getTotalCount();
	}

	@ExternalizedProperty
	public List<String> getInfoAttributes() {
		ReferenceDataSchema referenceDataSchema = survey.getReferenceDataSchema();
		SamplingPointDefinition samplingPoint = referenceDataSchema == null ? null: referenceDataSchema.getSamplingPointDefinition();
		if ( samplingPoint == null ) {
			return Collections.emptyList();
		} else {
			List<String> result = new ArrayList<String>();
			List<ReferenceDataDefinition.Attribute> infoAttributes = samplingPoint.getAttributes(false);
			for (ReferenceDataDefinition.Attribute attribute : infoAttributes) {
				result.add(attribute.getName());
			}
			return result;
		}
	}
	
}
