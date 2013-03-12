package org.openforis.collect.manager.referenceDataImport;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.openforis.collect.manager.referenceDataImport.ParsingError.ErrorType;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.commons.io.csv.CsvReader;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class CSVDataImportReader<T extends Line> extends DataImportReader<T> {
	
	protected CsvReader csvReader;
	protected CsvLine currentCSVLine;
	
	public CSVDataImportReader(String filename) throws IOException, ParsingException {
		super();
		csvReader = new CsvReader(filename);
		init();
	}

	public CSVDataImportReader(Reader reader) throws IOException, ParsingException {
		super();
		csvReader = new CsvReader(reader);
		init();
	}
	
	protected void init() throws IOException, ParsingException {
		csvReader.readHeaders();
		validateAllFile();
	}
	
	@Override
	public boolean isReady() {
		return currentCSVLine != null;
	}
	
	
	
	@Override
	public long getLinesRead() {
		return csvReader.getLinesRead();
	}
	
	public T readNextLine() throws ParsingException {
		try {
			currentCSVLine = csvReader.readNextLine();
			currentLine = parseCurrentLine();
			return currentLine;
		} catch (IOException e) {
			ParsingError error = new ParsingError(ErrorType.IOERROR, e.getMessage());
			throw new ParsingException(error);
		}
	}

	public List<String> getColumnNames() {
		return csvReader.getColumnNames();
	}

	public void close() throws IOException {
		csvReader.close();
	}
	
}
