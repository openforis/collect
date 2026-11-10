package org.openforis.collect.io.metadata.collectearth;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTestSurveyContext;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.collect.service.CollectCodeListService;
import org.openforis.idm.metamodel.CodeListService;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

public class CollectEarthBalloonPreviewControllerTest {

	private CollectEarthBalloonPreviewController controller;

	@Before
	public void init() throws Exception {
		// the survey is not stored: code list items are read from the survey itself
		CollectTestSurveyContext surveyContext = new CollectTestSurveyContext() {
			@Override
			public CodeListService getCodeListService() {
				CollectCodeListService codeListService = new CollectCodeListService();
				codeListService.setCodeListManager(new CodeListManager());
				return codeListService;
			}
		};
		CollectSurvey survey;
		try (InputStream is = getClass()
				.getResourceAsStream("/org/openforis/collect/designer/templates/collect_earth.idm.xml")) {
			survey = (CollectSurvey) new CollectSurveyIdmlBinder(surveyContext).unmarshal(is);
		}
		controller = new CollectEarthBalloonPreviewController();
		ReflectionTestUtils.setField(controller, "surveyManager", new SurveyManager() {
			@Override
			public CollectSurvey loadSurvey(int id) {
				return survey;
			}
		});
	}

	@Test
	public void testSurveyLanguage() throws Exception {
		assertTrue(preview("en").contains("font-family: \"en\""));
	}

	@Test
	public void testUnknownLanguageNotWrittenIntoPage() throws Exception {
		String html = preview("\"</style><script>alert(1)</script>");
		assertFalse(html.contains("<script>alert(1)"));
		assertTrue(html.contains("font-family: \"en\""));
	}

	private String preview(String lang) throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.showCollectEarthBalloonPreview(response, 1, lang);
		return response.getContentAsString();
	}
}
