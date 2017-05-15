package org.openforis.collect.io.metadata.parsing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.commons.io.csv.CsvLine;

public class CSVReferenceDataLineParser<L extends ReferenceDataLine> extends CSVLineParser<L> {

	private List<String> infoColumnNames;

	public CSVReferenceDataLineParser(DataImportReader<L> reader, CsvLine csvLine, List<String> infoColumnNames) {
		super(reader, csvLine);
		this.infoColumnNames = infoColumnNames;
	}
	
	@Override
	public L parse() throws ParsingException {
		L line = super.parse();
		line.setInfoAttributeByName(parseInfos(line));
		return line;
	}

	protected Map<String, String> parseInfos(L line) throws ParsingException {
		Map<String, String> result = new HashMap<String, String>();
		for (String columnName : infoColumnNames ) {
			String value = getColumnValue(columnName, false, String.class);
			if ( StringUtils.isNotBlank(value) ) {
				result.put(columnName, value);
			}
		}
		return result;
	}
}
