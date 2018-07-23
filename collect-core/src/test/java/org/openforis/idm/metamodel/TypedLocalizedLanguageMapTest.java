package org.openforis.idm.metamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.openforis.idm.metamodel.CodeListLabel.Type;

/**
 * 
 * @author S. Ricci
 *
 */
public class TypedLocalizedLanguageMapTest {
	
	@Test
	public void testAddEmptyLocaleText() {
		CodeListLabelMap map = new CodeListLabelMap();
		Type type = Type.ITEM;
		CodeListLabel label = new CodeListLabel(type, null, "Test");
		map.add(label);
		LanguageSpecificText reloaded = map.get(type, null);
		assertNotNull(reloaded);
		assertEquals(label, reloaded);
	}
	
	@Test
	public void testAddNotEmptyLocaleText() {
		CodeListLabelMap map = new CodeListLabelMap();
		String locale = "en_US";
		Type type = Type.ITEM;
		CodeListLabel label = new CodeListLabel(type, locale, "Test");
		map.add(label);
		LanguageSpecificText reloaded = map.get(type, locale);
		assertNotNull(reloaded);
		assertEquals(label, reloaded);
	}
	
	@Test
	public void testSetText() {
		CodeListLabelMap map = createComplexMap();
		Type type = Type.ITEM;
		String locale = "it_ITA";
		String newText = "Testo nuovo";
		map.setText(type, locale, newText);
		LanguageSpecificText reloaded = map.get(type, locale);
		assertNotNull(reloaded);
		assertEquals(newText, reloaded.getText());
	}
	
	@Test
	public void removeText() {
		CodeListLabelMap map = createComplexMap();
		List<CodeListLabel> oldValues = map.values();
		int oldSize = oldValues.size();
		String locale = "it_ITA";
		map.remove(Type.ITEM, locale);
		List<CodeListLabel> values = map.values();
		int size = values.size();
		assertEquals(oldSize - 1, size);
	}
	
	protected CodeListLabelMap createComplexMap() {
		CodeListLabelMap map = new CodeListLabelMap();
		addLabels(map, Type.ITEM);
		addLabels(map, Type.LIST);
		return map;
	}

	private void addLabels(CodeListLabelMap map, Type type) {
		CodeListLabel label = new CodeListLabel(type, null, "Test " + type.name());
		map.add(label);
		label = new CodeListLabel(type, "it_ITA", "Testo " + type.name());
		map.add(label);
		label = new CodeListLabel(type, "fr_FRA", "Texte " + type.name());
		map.add(label);
		label = new CodeListLabel(type, "sp_SPA", "Texto " + type.name());
		map.add(label);
	}
}
