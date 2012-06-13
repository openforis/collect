package org.openforis.collect.persistence.xml;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.SAXParser;
import org.openforis.collect.model.CollectRecord;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class DataUnmarshaller {

	private DataHandler handler;
	
	private final Log log = LogFactory.getLog(getClass());

	public DataUnmarshaller(DataHandler handler) {
		this.handler = handler;
	}
	
	private CollectRecord parse(InputSource source) throws DataUnmarshallerException {
		SAXParser p = new SAXParser();
		p.setContentHandler(handler);
		try {
			p.parse(source);
			List<String> failures = handler.getFailures();
			if ( failures.isEmpty() ) {
				return handler.getRecord();
			} else {
				throw new DataUnmarshallerException(failures);
			}
		} catch (SAXException e) {
			throw new DataUnmarshallerException(e);
		} catch (IOException e) {
			throw new DataUnmarshallerException(e);
		}
	}
	
	public CollectRecord parse(String filename) throws DataUnmarshallerException {
		FileReader reader = null;
		try {
			reader = new FileReader(filename);
			CollectRecord record = parse(reader); 
			reader.close();
			return record;
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
	
	public CollectRecord parse(Reader reader)  throws IOException, DataUnmarshallerException {
		InputSource is = new InputSource(reader);
		return parse(is);
	}

	public List<String> getLastParsingWarnings() {
		return handler.getWarnings();
	}
	
}
