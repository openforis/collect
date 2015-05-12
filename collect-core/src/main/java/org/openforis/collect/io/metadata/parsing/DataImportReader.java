package org.openforis.collect.io.metadata.parsing;

import java.io.Closeable;
import java.io.IOException;

import org.openforis.collect.io.exception.ParsingException;


/**
 * 
 * @author S. Ricci
 *
 * @param <T>
 */
public abstract class DataImportReader<T extends Line> implements Closeable {
	
	protected T currentLine;
	
	public abstract boolean isReady();
	
	public abstract boolean validateAllFile() throws ParsingException;
	
	public T getCurrentLine() {
		return currentLine;
	}
	
	public abstract T readNextLine() throws ParsingException;

	public abstract long getLinesRead();

	@Override
	public abstract void close() throws IOException;
	
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
