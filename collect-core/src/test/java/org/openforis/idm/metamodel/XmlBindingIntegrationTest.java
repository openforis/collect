/**
 * 
 */
package org.openforis.idm.metamodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import org.junit.Test;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;
import org.openforis.idm.metamodel.xml.internal.marshal.SurveyMarshaller;

import junit.framework.Assert;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class XmlBindingIntegrationTest {

	//private Logger log = Logger.getLogger(XmlBindingIntegrationTest.class);

	@Test
	public void roundTripMarshallingTest() throws IdmlParseException, IOException {
		URL idm = ClassLoader.getSystemResource("test.idm.xml");
		InputStream is = idm.openStream();
		SurveyContext ctx = new DefaultSurveyContext();
		SurveyIdmlBinder binder = new SurveyIdmlBinder(ctx);
		
		Survey survey = binder.unmarshal(is);
		
		StringWriter sw = new StringWriter();
		binder.marshal(survey, sw);
		String idml2 = sw.toString();

		StringReader sr = new StringReader(idml2);
		Survey survey2 = binder.unmarshal(sr);
		
		Assert.assertTrue(survey.deepEquals(survey2));
// TODO			
			new File("target/test/output").mkdirs();
			FileOutputStream fos = new FileOutputStream("target/test/output/marshalled.idm.xml");
			SurveyMarshaller sm = new SurveyMarshaller(binder);
//			sm.setIndent(true);
			sm.marshal(survey, fos, "UTF-8");
			fos.flush();
			fos.close();
	}
	
	public void validateValidIdmlTest() {
		// TODO
//		ValidationEvent[] validationEvents = e.getValidationEvents();
//		for (ValidationEvent validationEvent : validationEvents) {
//			log.error(validationEvent.getMessage());
//		}
	}
}
