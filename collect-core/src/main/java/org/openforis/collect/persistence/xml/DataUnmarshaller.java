package org.openforis.collect.persistence.xml;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.xml.DataHandler.NodeUnmarshallingError;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class DataUnmarshaller {

	private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
	private static final Log log = LogFactory.getLog(DataUnmarshaller.class);

	private DataHandler handler;

	public DataUnmarshaller(DataHandler handler) {
		this.handler = handler;
	}
	
	private ParseRecordResult parse(InputSource source) throws IOException {
		ParseRecordResult result = new ParseRecordResult();
		try {
			XMLReader reader = createReader();
			reader.parse(source);
			List<NodeUnmarshallingError> failures = handler.getFailures();
			if ( failures.isEmpty() ) {
				CollectRecord record = handler.getRecord();
				result.setRecord(record);
				List<NodeUnmarshallingError> warns = handler.getWarnings();
				if (warns.size() > 0) {
					result.setMessage("Processed with errors: " + warns.toString());
					result.setWarnings(warns);
				}
				result.setSuccess(true);
			} else {
				result.setFailures(failures);
			}
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			NodeUnmarshallingError error = new NodeUnmarshallingError(e.toString());
			result.setFailures(Arrays.asList(error));
		}
		return result;
	}

	protected XMLReader createReader() throws ParserConfigurationException, SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		// create a parser
		SAXParser parser = factory.newSAXParser();
		// create the reader (scanner)
		XMLReader reader = parser.getXMLReader();
		reader.setFeature(NAMESPACES_FEATURE, true);
		reader.setContentHandler(handler);
		return reader;
	}
	
	public ParseRecordResult parse(String filename) throws DataUnmarshallerException {
		FileReader reader = null;
		try {
			reader = new FileReader(filename);
			ParseRecordResult result = parse(reader); 
			reader.close();
			return result;
		} catch (IOException e) {
			throw new DataUnmarshallerException(e);
		} finally {
			if ( reader != null ) {
				try {
					reader.close();
				} catch (IOException e) {
					log.warn("Failed to close Reader: "+e);
				}
			}
		}
	}
	
	public ParseRecordResult parse(Reader reader) throws IOException {
		InputSource is = new InputSource(reader);
		return parse(is);
	}

	public class ParseRecordResult {
		
		private boolean success;
		private String message;
		private List<NodeUnmarshallingError> warnings;
		private List<NodeUnmarshallingError> failures;
		private CollectRecord record;

		public ParseRecordResult() {
		}
		
		public ParseRecordResult(CollectRecord record) {
			this();
			this.record = record;
		}

		public boolean hasWarnings() {
			return warnings != null && warnings.size() > 0;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public CollectRecord getRecord() {
			return record;
		}

		public void setRecord(CollectRecord record) {
			this.record = record;
		}

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public List<NodeUnmarshallingError> getWarnings() {
			return warnings;
		}

		public void setWarnings(List<NodeUnmarshallingError> warnings) {
			this.warnings = warnings;
		}

		public List<NodeUnmarshallingError> getFailures() {
			return failures;
		}

		public void setFailures(List<NodeUnmarshallingError> failures) {
			this.failures = failures;
		}

	}
	
}
