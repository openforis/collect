package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.APPLICATION_OPTIONS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.CYCLE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DESCRIPTION;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LANGUAGE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.PROJECT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.REFERENCE_DATA_SCHEMA;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SCHEMA;
import static org.openforis.idm.metamodel.xml.IdmlConstants.SPATIAL_REFERENCE_SYSTEMS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.UNITS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.URI;
import static org.openforis.idm.metamodel.xml.IdmlConstants.VERSIONING;

import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.CodeListImporter;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyCodeListImporterPR extends SurveyUnmarshaller {
	
	private CodeListImporter importer;

	public SurveyCodeListImporterPR(CodeListImporter importer, Survey survey) {
		super(null, false);

		if ( survey == null ) {
			throw new NullPointerException("survey");
		}
		
		this.importer = importer;
		this.survey = survey;
	}

	@Override
	protected void initChildren() {
		addChildPullReaders(
				new SkipElementPR(PROJECT), 
				new SkipElementPR(URI), 
				new SkipElementPR(CYCLE),
				new SkipElementPR(DESCRIPTION),
				new SkipElementPR(LANGUAGE),
				new SkipElementPR(APPLICATION_OPTIONS),
				new SkipElementPR(VERSIONING), 
				new CodeListsPersisterPR(),
				new SkipElementPR(UNITS),
				new SkipElementPR(SPATIAL_REFERENCE_SYSTEMS),
				new SkipElementPR(REFERENCE_DATA_SCHEMA),
				new SkipElementPR(SCHEMA)
		);
	}
	
	@Override
	protected void initSurvey() {
		//skip it, survey is passed as parameter to the constructor
	}
	
	public CodeListImporter getImporter() {
		return importer;
	}
	
	@Override
	public Survey getSurvey() {
		return survey;
	}
}
