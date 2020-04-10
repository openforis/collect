package org.openforis.collect.io.metadata.parsing;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.io.parsing.CSVFileOptions;
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
		this(new File(filename));
	}

	public CSVDataImportReader(File file) throws IOException, ParsingException {
		super();
		csvReader = new CsvReader(file);
	}

	public CSVDataImportReader(File file, CSVFileOptions csvFileOptions) throws IOException, ParsingException {
		super();
		if (csvFileOptions == null) {
			csvFileOptions = new CSVFileOptions();
		}
		csvReader = new CsvReader(file, csvFileOptions.getCharset().getCharsetName(), 
				csvFileOptions.getSeparator().getCharacter(), 
				csvFileOptions.getTextDelimiter().getCharacter());
	}

	@Deprecated
	public CSVDataImportReader(Reader reader) throws IOException, ParsingException {
		super();
		csvReader = new CsvReader(reader);
	}
	
	public void init() throws IOException, ParsingException {
		readHeaders();
		validateAllFile();
	}

	protected void readHeaders() throws IOException {
		csvReader.readHeaders();
	}
	
	@Override
	public boolean isReady() {
		return currentCSVLine != null;
	}
	
	@Override
	public long getLinesRead() {
		return csvReader.getLinesRead();
	}
	
	public int size() throws IOException {
		return csvReader.size();
	}
	
	@Override
	public void close() throws IOException {
		csvReader.close();
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

}
