package org.openforis.idm.metamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

/**
 * 
 * @author S. Ricci
 *
 */
public class LocalizedLanguageMapTest {
	
	@Test
	public void testAddEmptyLocaleText() {
		LanguageSpecificTextMap map = new LanguageSpecificTextMap();
		LanguageSpecificText languageSpecificText = new LanguageSpecificText(null, "Test");
		map.add(languageSpecificText);
		LanguageSpecificText reloaded = map.get(null);
		assertNotNull(reloaded);
		assertEquals(languageSpecificText, reloaded);
	}
	
	@Test
	public void testAddNotEmptyLocaleText() {
		LanguageSpecificTextMap map = new LanguageSpecificTextMap();
		LanguageSpecificText languageSpecificText = new LanguageSpecificText("en_US", "Test");
		map.add(languageSpecificText);
		LanguageSpecificText reloaded = map.get("en_US");
		assertNotNull(reloaded);
		assertEquals(languageSpecificText, reloaded);
	}
	
	@Test
	public void testSetText() {
		LanguageSpecificTextMap map = createComplexMap();
		map.setText("it_ITA", "Testo nuovo");
		LanguageSpecificText reloaded = map.get("it_ITA");
		assertNotNull(reloaded);
		assertEquals("Testo nuovo", reloaded.getText());
	}
	
	@Test
	public void removeText() {
		LanguageSpecificTextMap map = createComplexMap();
		List<LanguageSpecificText> oldValues = map.values();
		int oldSize = oldValues.size();
		map.remove("it_ITA");
		List<LanguageSpecificText> values = map.values();
		int size = values.size();
		assertEquals(oldSize - 1, size);
	}
	
	protected LanguageSpecificTextMap createComplexMap() {
		LanguageSpecificTextMap map = new LanguageSpecificTextMap();
		LanguageSpecificText languageSpecificText = new LanguageSpecificText(null, "Text");
		map.add(languageSpecificText);
		languageSpecificText = new LanguageSpecificText("it_ITA", "Testo");
		map.add(languageSpecificText);
		languageSpecificText = new LanguageSpecificText("fr_FRA", "Texte");
		map.add(languageSpecificText);
		languageSpecificText = new LanguageSpecificText("sp_SPA", "Texto");
		map.add(languageSpecificText);
		return map;
	}

}
