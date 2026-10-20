package org.openforis.collect.io.metadata.collectearth.balloon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.openforis.collect.io.metadata.collectearth.balloon.CEField.CEFieldType;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;

/**
 * Qualifiable code list items ( "Other, specify" ) in the Collect Earth balloon. The balloon had no input for their text, so
 * Collect Earth never received it and the plot was reported as missing it.
 */
public class CEComponentHTMLFormatterQualifierTest {

	private static final String FIELD = "collect_code_disturbance";

	private final CodeList list = new CollectSurveyContext().createSurvey().createCodeList();

	private CodeListItem item(String code, boolean qualifiable) {
		CodeListItem item = list.createItem(1);
		item.setCode(code);
		item.setLabel("en", code);
		item.setQualifiable(qualifiable);
		return item;
	}

	private String format(CEFieldType type, CodeListItem... items) {
		Map<Integer, List<CodeListItem>> itemsByParentId = new HashMap<Integer, List<CodeListItem>>();
		itemsByParentId.put(0, Arrays.asList(items));
		CECodeField field = new CECodeField(FIELD, "disturbance", "Disturbance", null, type, false, false, itemsByParentId, null);
		CETab tab = new CETab("main", "Main");
		tab.addChild(field);
		CETabSet tabSet = new CETabSet("tabs", "Tabs");
		tabSet.addTab(tab);
		return new CEComponentHTMLFormatter("en").format(tabSet);
	}

	@Test
	public void aCodeFieldWithAQualifiableItemGetsAnInputForItsText() {
		String html = format(CEFieldType.CODE_BUTTON_GROUP, item("fire", false), item("other", true));

		assertTrue(html, html.contains("id=\"" + FIELD + "_qualifier\""));
		assertTrue(html, html.contains("data-qualifier-for=\"" + FIELD + "\""));
		assertTrue(html, html.contains("data-qualifiable-codes=\"[&quot;other&quot;]\""));
		assertTrue(html, html.contains("placeholder=\"Specify\""));
		assertTrue("Hidden until the item is selected", html.contains("display: none"));
	}

	@Test
	public void theInputHasNoNameSoItIsNeverSentAsAParameterOfItsOwn() {
		String html = format(CEFieldType.CODE_SELECT, item("other", true));

		Matcher input = Pattern.compile("<input[^>]*code-qualifier[^>]*>").matcher(html);
		assertTrue(html, input.find());
		assertFalse(input.group(), input.group().contains("name="));
	}

	@Test
	public void aCodeFieldWithoutQualifiableItemsIsUnchanged() {
		String html = format(CEFieldType.CODE_BUTTON_GROUP, item("fire", false), item("logging", false));

		assertFalse(html, html.contains("code-qualifier"));
	}

	@Test
	public void theQualifiableCodesAreWrittenAsAJsonArray() {
		assertEquals("[\"other\",\"a\\\"b\",\"c\\\\d\"]",
				CEComponentHTMLFormatter.toJsonArray(Arrays.asList("other", "a\"b", "c\\d")));
	}

	@Test
	public void theQualifiableCodesOfEveryParentAreCollected() {
		Map<Integer, List<CodeListItem>> itemsByParentId = new HashMap<Integer, List<CodeListItem>>();
		itemsByParentId.put(1, Arrays.asList(item("other", true), item("pine", false)));
		itemsByParentId.put(2, Arrays.asList(item("other", true), item("unknown", true)));
		CECodeField field = new CECodeField(FIELD, "species", "Species", null, CEFieldType.CODE_SELECT, false, false,
				itemsByParentId, "collect_code_genus");

		assertEquals(Arrays.asList("other", "unknown"), CEComponentHTMLFormatter.getQualifiableCodes(field));
	}
}
