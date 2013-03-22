package org.openforis.collect.manager.referencedataimport;


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
	
	protected T parseCurrentLine() throws ParsingException {
		if ( isReady() ) {
			LineParser<T> lineParser = createLineParserInstance();
			return lineParser.parse();
		} else {
			return null;
		}
	}

	protected abstract LineParser<T> createLineParserInstance();
	
}
