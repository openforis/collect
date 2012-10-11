package org.openforis.collect.persistence.xml;

import static org.openforis.collect.metamodel.ui.UIOptionsConstants.*;

import java.io.IOException;
import java.util.List;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.xml.IdmlConstants;
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
		super(UI_NAMESPACE_URI, TAB_SETS);
		
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
			super(TAB_SET);
			
			addChildPullReaders(
				new TabPR()
			);
		}
		
		@Override
		protected void onStartTag() throws XmlParseException,
				XmlPullParserException, IOException {
			tabSet = new UITabSet();
			tabSet.setName(getAttribute(NAME, true));
			
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
			super(UI_NAMESPACE_URI, tabName);
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
			String name = getAttribute(NAME, false);
			tabSet = new UITab();
			tabSet.setName(name);
		}
		
		@Override
		protected void onEndTag() throws XmlParseException {
			parentTabSet.addTab((UITab) tabSet);
		}
		
		private class LabelPR extends LanguageSpecificTextPR {

			public LabelPR() {
				super(LABEL);
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
			super(UI_NAMESPACE_URI, tagName);
			this.requireType = requireType;
		}
		
		public LanguageSpecificTextPR(String tagName) {
			this(tagName, false);
		}

		@Override
		protected void onStartTag() throws XmlParseException, XmlPullParserException, IOException {
			XmlPullParser parser = getParser();
			String lang = parser.getAttributeValue(UI_NAMESPACE_URI, IdmlConstants.XML_LANG_ATTRIBUTE);
			String type = getAttribute(TYPE, requireType);
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
