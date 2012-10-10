package org.openforis.collect.persistence.xml;

import java.io.IOException;
import java.util.List;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.openforis.idm.metamodel.xml.internal.marshal.XmlPullReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 * @author S. Ricci
 *
 */
public class UIOptionsPullReader extends XmlPullReader {

	private UIOptions uiOptions;

	public UIOptionsPullReader() {
		super(UIOptions.UI_NAMESPACE_URI, "tabSets");
		
		addChildPullReaders(
			new TabSetPR()
			);
	}
	
	@Override
	protected void onStartTag() throws XmlParseException,
			XmlPullParserException, IOException {
		this.uiOptions = new UIOptions();
	}
	
	public UIOptions getUIOptions() {
		return uiOptions;
	}

	private class TabSetPR extends TabSetPRBase {

		public TabSetPR() {
			super("tabSet");
			
			addChildPullReaders(
				new TabPR()
			);
		}
		
		@Override
		protected void onStartTag() throws XmlParseException,
				XmlPullParserException, IOException {
			tabSet = new UITabSet();
			tabSet.setName(getAttribute("name", true));
			
			setParentSetInChildren(tabSet);
		}
		
		@Override
		protected void onEndTag() throws XmlParseException {
			uiOptions.addTabSet((UITabSet) tabSet);
		}
		
		private void setParentSetInChildren(UITabSet tabSet) {
			List<XmlPullReader> childprs = getChildPullReaders();
			for (XmlPullReader pr : childprs) {
				if ( pr instanceof TabSetPRBase ) {
					((TabSetPRBase) pr).parentTabSet = tabSet;
				}
			}
		}

	}
	
	private abstract class TabSetPRBase extends XmlPullReader {
		
		protected UITabSet parentTabSet;
		protected UITabSet tabSet;

		public TabSetPRBase(String tabName) {
			super(UIOptions.UI_NAMESPACE_URI, tabName);
		}
		
		@Override
		protected final void handleChildTag(XmlPullReader childPR)
				throws XmlPullParserException, IOException, XmlParseException {
			
			if ( childPR instanceof TabPR ) {
				TabPR tabSetPR = (TabPR) childPR;
				// Store child state in case reused recursively
				UITabSet tmpParentTabSet = tabSetPR.parentTabSet;
				UITabSet tmpTabSet = tabSetPR.tabSet;
				tabSetPR.parentTabSet = tabSet;
				tabSetPR.tabSet = null;
				super.handleChildTag(childPR);
				tabSetPR.parentTabSet = tmpParentTabSet;
				tabSetPR.tabSet = tmpTabSet;
			} else {
				super.handleChildTag(childPR);
			}
		}
	}
	
	private class TabPR extends TabSetPRBase {
		
		public TabPR() {
			super("tab");
			
			addChildPullReaders(
				new LabelPR(),
				this
				);
		}
		
		@Override
		protected void onStartTag() throws XmlParseException, XmlPullParserException, IOException {
			String name = getAttribute("name", false);
			tabSet = new UITab();
			tabSet.setName(name);
		}
		
		@Override
		protected void onEndTag() throws XmlParseException {
			parentTabSet.addTab((UITab) tabSet);
		}
		
		private class LabelPR extends LanguageSpecificTextPR {
			public LabelPR() {
				super("label");
			}
			
			@Override
			protected void processText(LanguageSpecificText lst) {
				((UITab) tabSet).addLabel(lst);
			}
		}
	}
	
	private abstract class LanguageSpecificTextPR extends XmlPullReader {
		private boolean requireType;
		
		public LanguageSpecificTextPR(String tagName, boolean requireType) {
			super(UIOptions.UI_NAMESPACE_URI, tagName);
			this.requireType = requireType;
		}
		
		public LanguageSpecificTextPR(String tagName) {
			this(tagName, false);
		}

		@Override
		protected void onStartTag() throws XmlParseException, XmlPullParserException, IOException {
			XmlPullParser parser = getParser();
			String lang = parser.getAttributeValue(UIOptions.UI_NAMESPACE_URI, "lang");
			String type = getAttribute("type", requireType);
			String text = parser.nextText();
			processText(lang, type, text);
		}

		/** 
		 * Override this method to handle "type" attribute for other label types
		 * @param lang
		 * @param type
		 * @param text
		 * @throws XmlParseException 
		 */
		protected void processText(String lang, String type, String text) throws XmlParseException {
			LanguageSpecificText lst = new LanguageSpecificText(lang, text.trim());
			processText(lst);
		}
		
		protected void processText(LanguageSpecificText lst) {
			// no-op
		}
	}
}
