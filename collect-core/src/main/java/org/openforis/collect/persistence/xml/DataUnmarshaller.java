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
 *
 */
public class DataUnmarshaller {

//	private CollectSurvey survey;
//	private CollectRecordContext recordContext;
	private DataHandler handler;
	
	private final Log log = LogFactory.getLog(getClass());

//	public DataUnmarshaller(CollectSurvey survey, CollectRecordContext recordContext) {
//		super();
//		this.survey = survey;
//		this.recordContext = recordContext;
//	}

	public DataUnmarshaller(DataHandler handler) {
		this.handler = handler;
	}
	
	private CollectRecord parse(InputSource source) throws DataUnmarshallerException {
		SAXParser p = new SAXParser();
//		DataHandler handler = new DataHandler(recordContext, survey);
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
	
//	public static void main(String[] args) {
//		try {
//			
//			// Load IDML
//			CollectIdmlBindingContext idmlBindingContext = new CollectIdmlBindingContext();
//			SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
//			CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal("/home/gino/workspace/faofin/tz/naforma-idm/tanzania-naforma.idm.xml");
//			// Load record
//			long start = System.currentTimeMillis();
//			DataHandler handler = new DataHandler(survey);
//			DataUnmarshaller dataUnmarshaller = new DataUnmarshaller(handler);
//			CollectRecord record = dataUnmarshaller.parse("/home/gino/workspace/temp/tzdata/data/3/143_169/data.xml");
//			long end = System.currentTimeMillis();
//			System.out.println(record);
//			System.out.println("Loaded in "+(end-start)+"ms");
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InvalidIdmlException e) {
//			e.printStackTrace();
//		} catch (DataUnmarshallerException e) {
//			e.printStackTrace();
//		}
//	}
}
