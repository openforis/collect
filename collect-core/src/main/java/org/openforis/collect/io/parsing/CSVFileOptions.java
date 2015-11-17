package org.openforis.collect.io.parsing;

/**
 * 
 * @author S. Ricci
 *
 */
public class CSVFileOptions {

	public static final FileCharset DEFAULT_CHARSET = FileCharset.UTF_8;
	private static final CSVFileSeparator DEFAULT_SEPARATOR = CSVFileSeparator.COMMA;
	private static final CSVFileTextDelimiter DEFAULT_TEXT_DELIMITER = CSVFileTextDelimiter.DOUBLE_QUOTE;
	
	private FileCharset charset;
	private CSVFileSeparator separator;
	private CSVFileTextDelimiter textDelimiter;
	
	public CSVFileOptions() {
		charset = DEFAULT_CHARSET;
		separator = DEFAULT_SEPARATOR;
		textDelimiter = DEFAULT_TEXT_DELIMITER;
	}
	
	public CSVFileOptions(FileCharset charset,
			CSVFileSeparator separator, CSVFileTextDelimiter textDelimiter) {
		super();
		this.charset = charset;
		this.separator = separator;
		this.textDelimiter = textDelimiter;
	}
	
	public FileCharset getCharset() {
		return charset;
	}
	
	public void setCharset(FileCharset charset) {
		this.charset = charset;
	}
	
	public CSVFileSeparator getSeparator() {
		return separator;
	}

	public void setSeparator(CSVFileSeparator separator) {
		this.separator = separator;
	}
	
	public CSVFileTextDelimiter getTextDelimiter() {
		return textDelimiter;
	}
	
	public void setTextDelimiter(CSVFileTextDelimiter textDelimiter) {
		this.textDelimiter = textDelimiter;
	}
	
}
