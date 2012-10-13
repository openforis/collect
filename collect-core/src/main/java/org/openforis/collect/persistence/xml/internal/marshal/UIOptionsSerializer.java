package org.openforis.collect.persistence.xml.internal.marshal;

import static org.openforis.collect.metamodel.ui.UIOptionsConstants.LABEL;
import static org.openforis.collect.metamodel.ui.UIOptionsConstants.NAME;
import static org.openforis.collect.metamodel.ui.UIOptionsConstants.TAB;
import static org.openforis.collect.metamodel.ui.UIOptionsConstants.TAB_SET;
import static org.openforis.collect.metamodel.ui.UIOptionsConstants.UI_NAMESPACE_URI;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.xml.IdmlConstants;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 * 
 * @author S. Ricci
 *
 */
public class UIOptionsSerializer {

	private static final String INDENT = "http://xmlpull.org/v1/doc/features.html#indent-output";

	public void write(UIOptions options, Writer out) {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlSerializer serializer = factory.newSerializer();
			serializer.setFeature(INDENT, true);
			serializer.setOutput(out);
			serializer.startDocument("UTF-8", true);
			serializer.setPrefix("ui", UI_NAMESPACE_URI);
			List<UITabSet> tabSets = options.getTabSets();
			for (UITabSet tabSet : tabSets) {
				writeTabSet(serializer, tabSet);
			}
			serializer.endDocument();
			serializer.flush();
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
	}

	protected void writeTabSet(XmlSerializer serializer, UITabSet tabSet)
			throws IOException {
		serializer.startTag(UI_NAMESPACE_URI, TAB_SET);
		serializer.attribute(UI_NAMESPACE_URI, NAME	, tabSet.getName());
		List<UITab> tabs = tabSet.getTabs();
		for (UITab tab : tabs) {
			writeTab(serializer, tab);
		}
		serializer.endTag(UI_NAMESPACE_URI, TAB_SET);
	}

	protected void writeTab(XmlSerializer serializer, UITab tab) throws IOException {
		serializer.startTag(UI_NAMESPACE_URI, TAB);
		serializer.attribute(UI_NAMESPACE_URI, NAME	, tab.getName());
		List<LanguageSpecificText> labels = tab.getLabels();
		for (LanguageSpecificText label : labels) {
			writeLabel(serializer, label);
		}
		List<UITab> tabs = tab.getTabs();
		for (UITab innerTab : tabs) {
			writeTab(serializer, innerTab);
		}
		serializer.endTag(UI_NAMESPACE_URI, TAB);
	}

	protected void writeLabel(XmlSerializer serializer, LanguageSpecificText label)
			throws IOException {
		serializer.startTag(UI_NAMESPACE_URI, LABEL);
		serializer.attribute(UI_NAMESPACE_URI, IdmlConstants.XML_LANG_ATTRIBUTE, label.getLanguage());
		serializer.text(label.getText());
		serializer.endTag(UI_NAMESPACE_URI, LABEL);
	}
}
