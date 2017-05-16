package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.ATTRIBUTE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.KEY;
import static org.openforis.idm.metamodel.xml.IdmlConstants.NAME;
import static org.openforis.idm.metamodel.xml.IdmlConstants.REFERENCE_DATA_SCHEMA;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SAMPLING_POINT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.TAXONOMY;

import java.io.IOException;
import java.util.List;

import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.TaxonomyDefinition;
import org.openforis.idm.metamodel.Survey;

/**
 * 
 * @author S. Ricci
 *
 */
class ReferenceDataSchemaXS extends XmlSerializerSupport<ReferenceDataSchema, Survey> {

	ReferenceDataSchemaXS() {
		super(REFERENCE_DATA_SCHEMA);
		addChildMarshallers(
			new SamplingPointXS(),
			new TaxonomyXS()
		);
	}

	@Override
	protected void marshalInstances(Survey survey) throws IOException {
		ReferenceDataSchema schema = survey.getReferenceDataSchema();
		marshal(schema);
	}

	private class SamplingPointXS extends XmlSerializerSupport<SamplingPointDefinition, ReferenceDataSchema> {
		
		public SamplingPointXS() {
			super(SAMPLING_POINT);
			addChildMarshallers(
				new AttributeXS()
			);
		}
		
		@Override
		protected void marshalInstances(ReferenceDataSchema schema) throws IOException {
			SamplingPointDefinition samplingPoint = schema.getSamplingPointDefinition();
			marshal(samplingPoint);
		}

	}

	private class TaxonomyXS extends XmlSerializerSupport<TaxonomyDefinition, ReferenceDataSchema> {
		
		public TaxonomyXS() {
			super(TAXONOMY);
			addChildMarshallers(
				new AttributeXS()
			);
		}
		
		@Override
		protected void marshalInstances(ReferenceDataSchema schema) throws IOException {
			List<TaxonomyDefinition> taxonDefinitions = schema.getTaxonomyDefinitions();
			marshal(taxonDefinitions);
		}
		
		@Override
		protected void attributes(TaxonomyDefinition taxonDefinition) throws IOException {
			attribute(NAME, taxonDefinition.getTaxonomyName());
		}
	}

	private class AttributeXS extends XmlSerializerSupport<ReferenceDataDefinition.Attribute, ReferenceDataDefinition> {

		public AttributeXS() {
			super(ATTRIBUTE);
		}
		
		@Override
		protected void marshalInstances(ReferenceDataDefinition defn) throws IOException {
			marshal(defn.getAttributes());
		}
		
		@Override
		protected void attributes(ReferenceDataDefinition.Attribute attribute) throws IOException {
			attribute(NAME, attribute.getName());
			attribute(KEY, attribute.isKey(), false);
		}
	}
	
}
