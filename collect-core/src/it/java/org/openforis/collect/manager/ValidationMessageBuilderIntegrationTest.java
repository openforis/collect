package org.openforis.collect.manager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;

/**
 * 
 * @author S. Ricci
 *
 */
public class ValidationMessageBuilderIntegrationTest extends CollectIntegrationTest {
	
//	private CollectSurvey survey;
	
//	@BeforeClass
//	private void init() throws IdmlParseException {
//		this.survey = loadSurvey();
//	}
	
	@Test
	public void defaultLanguageMessageTest() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		{
			String message = messageSource.getMessage(Locale.US, "validation.codeError");
			assertNotNull(message);
		}
		//not existent message
		{
			String message = messageSource.getMessage(Locale.ITALY, "validation.codeError");
			assertNull(message);
		}
	}
	
}
