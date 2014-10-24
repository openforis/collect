package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

import java.io.IOException;
import java.util.List;

import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.metamodel.Survey;

/**
 * 
 * @author G. Miceli
 *
 */
class SpatialReferenceSystemsXS extends XmlSerializerSupport<SpatialReferenceSystem, Survey> {

	SpatialReferenceSystemsXS() {
		super(SPATIAL_REFERENCE_SYSTEM);
		setListWrapperTag(SPATIAL_REFERENCE_SYSTEMS);
		addChildMarshallers(
				new LabelXS(),
				new DescriptionXS(),
				new WktXS());
	}
	
	@Override
	protected void marshalInstances(Survey survey) throws IOException {
		List<SpatialReferenceSystem> srss = survey.getSpatialReferenceSystems();
		marshal(srss);
	}
	
	@Override
	protected void attributes(SpatialReferenceSystem srs) throws IOException {
		attribute(SRID, srs.getId());
	}
	
	private class LabelXS extends LanguageSpecificTextXS<SpatialReferenceSystem> {

		public LabelXS() {
			super(LABEL);
		}
		
		@Override
		protected void marshalInstances(SpatialReferenceSystem srs) throws IOException {
			marshal(srs.getLabels());
		}
	}
	
	private class DescriptionXS extends LanguageSpecificTextXS<SpatialReferenceSystem> {

		public DescriptionXS() {
			super(DESCRIPTION);
		}
		
		@Override
		protected void marshalInstances(SpatialReferenceSystem srs) throws IOException {
			marshal(srs.getDescriptions());
		}
	}
	
	private class WktXS extends XmlSerializerSupport<String, SpatialReferenceSystem> {

		public WktXS() {
			super(WKT);
		}
		
		@Override
		protected void marshalInstances(SpatialReferenceSystem srs) throws IOException {
			marshal(srs.getWellKnownText());
		}
		
		@Override
		protected void body(String wkt) throws IOException {
			cdsect(wkt);
		}
	}
}
