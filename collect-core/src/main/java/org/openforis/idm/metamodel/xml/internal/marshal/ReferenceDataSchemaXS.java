package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.ATTRIBUTE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.KEY;
import static org.openforis.idm.metamodel.xml.IdmlConstants.NAME;
import static org.openforis.idm.metamodel.xml.IdmlConstants.REFERENCE_DATA_SCHEMA;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SAMPLING_POINT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.TAXON;

import java.io.IOException;

import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.TaxonDefinition;
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
			new TaxonXS()
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

	private class TaxonXS extends XmlSerializerSupport<TaxonDefinition, ReferenceDataSchema> {
		
		public TaxonXS() {
			super(TAXON);
			addChildMarshallers(
				new AttributeXS()
			);
		}
		
		@Override
		protected void marshalInstances(ReferenceDataSchema schema) throws IOException {
			TaxonDefinition taxon = schema.getTaxonDefinition();
			marshal(taxon);
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
