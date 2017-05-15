package org.openforis.collect.io.metadata.parsing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.parsing.CSVFileOptions;
import org.openforis.collect.utils.SurveyObjects;

public abstract class CSVReferenceDataImportReader<L extends ReferenceDataLine> extends CSVDataImportReader<L> {

	protected List<String> infoColumnNames = new ArrayList<String>();
	
	public CSVReferenceDataImportReader(File file) throws IOException, ParsingException {
		super(file);
	}

	public CSVReferenceDataImportReader(File file, CSVFileOptions csvFileOptions) throws IOException, ParsingException {
		super(file, csvFileOptions);
	}

	@Override
	public void init() throws IOException, ParsingException {
		super.init();
		List<String> columnNames = csvReader.getColumnNames();
		for (String col : columnNames) {
			String adjustedName = SurveyObjects.adjustInternalName(col);
			if ( isInfoAttribute(adjustedName) ) {
				infoColumnNames.add(adjustedName);
			}
		}
	}
	
	protected abstract boolean isInfoAttribute(String col);
	
	public List<String> getInfoColumnNames() {
		return infoColumnNames;
	}
}
