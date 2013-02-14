package org.openforis.collect.manager.referenceDataImport;



/**
 * 
 * @author S. Ricci
 *
 * @param <T>
 */
public abstract class DataImportReader<T extends Line> {
	
	protected T currentLine;
	
	public abstract boolean isReady();
	
	public abstract boolean validateAllFile() throws ParsingException;
	
	public T getCurrentLine() {
		return currentLine;
	}
	
	public abstract T readNextLine() throws ParsingException;

	public abstract long getLinesRead();
	
	protected abstract T parseCurrentLine() throws ParsingException;

	protected abstract LineParser<T> createLineParserInstance();
	
}
