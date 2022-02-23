package org.openforis.collect.persistence.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptionsConstants;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.idm.metamodel.Survey;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class UIOptionsBinderTest extends CollectIntegrationTest {
	
	@Autowired
	private CollectSurveyContext context;
	
	@Test
	public void testUnmarshall() throws IOException {
		String optionsBody = loadTestOptions();
		UIOptionsBinder binder = new UIOptionsBinder();
		Survey survey = createTestSurvey();
		UIOptions uiOptions = binder.unmarshal(survey, UIOptionsConstants.UI_TYPE, optionsBody);
		assertNotNull(uiOptions);
		List<UITabSet> tabSets = uiOptions.getTabSets();
		assertEquals(1, tabSets.size());
		UITabSet clusterRootTabSet = tabSets.get(0);
		assertEquals("cluster", clusterRootTabSet.getName());
		List<UITab> tabs = clusterRootTabSet.getTabs();
		assertEquals(4, tabs.size());
		UITab plotTab = tabs.get(1);
		assertEquals("plot", plotTab.getName());
		String label = plotTab.getLabel("en");
		assertEquals("Plot", label);
		assertEquals(clusterRootTabSet, plotTab.getParent());
		List<UITab> plotInnerTabs = plotTab.getTabs();
		assertEquals(6, plotInnerTabs.size());
		UITab plotDetTab = plotInnerTabs.get(0);
		assertEquals("plot_det", plotDetTab.getName());
	}
	
	@Test
	public void roundTripMarshallingTest() throws IOException {
		String optionsBody = loadTestOptions();
		UIOptionsBinder binder = new UIOptionsBinder();
		Survey survey = createTestSurvey();
		UIOptions uiOptions = binder.unmarshal(survey, UIOptionsConstants.UI_TYPE, optionsBody);
		new File("target/test/output").mkdirs();
		FileOutputStream fos = new FileOutputStream("target/test/output/marshalled.uioptions.xml");
		String marshalled = binder.marshal(uiOptions, survey.getDefaultLanguage());
		assertNotNull(marshalled);
		IOUtils.write(marshalled, fos, OpenForisIOUtils.UTF_8);
		fos.flush();
		fos.close();
	}
	
	private String loadTestOptions() throws IOException {
		URL fileUrl = ClassLoader.getSystemResource("test-uioptions.xml");
		InputStream is = fileUrl.openStream();
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		String result = writer.toString();
		return result;
	}
	
	private Survey createTestSurvey() {
		Survey survey = context.createSurvey();
		survey.setName("test");
		survey.setUri("http://www.openforis.org/test");
		survey.addLanguage("en");
		return survey;
	}
	
}
