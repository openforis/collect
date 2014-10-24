package org.openforis.idm.metamodel.xml.internal.unmarshal;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.Prompt;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParserException;
import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

/**
 * @author G. Miceli
 */
abstract class NodeDefinitionPR extends IdmlPullReader {
	private NodeDefinition definition;
	private EntityDefinition parentDefinition;
	private Schema schema;
	
	public NodeDefinitionPR(String tagName) {
		super(tagName);
		setUnordered(true);
		addChildPullReaders(
				new LabelPR(), 
				new DescriptionPR(),
				new PromptPR()
			);
	}
	
	protected NodeDefinition getDefinition() {
		return definition;
	}
	
	protected EntityDefinition getParentDefinition() {
		return parentDefinition;
	}

	public Schema getSchema() {
		return schema;
	}
	
	@Override
	protected final void onStartTag()
			throws XmlParseException, XmlPullParserException,
			IOException {				
		schema = getSurvey().getSchema(); 
		int id = getIntegerAttribute(ID, true);
		this.definition = createDefinition(id);
		
		String name = getAttribute(NAME, true);
		String since = getAttribute(SINCE, false);
		String deprecated = getAttribute(DEPRECATED, false);
		Boolean required = getBooleanAttribute(REQUIRED, false);
		String requiredIf = getAttribute(REQUIRED_IF, false);
		String relevant = getAttribute(RELEVANT, false);
		Integer minCount = getIntegerAttribute(MIN_COUNT, false);
		Boolean multiple = getBooleanAttribute(MULTIPLE, false);
		if ( parentDefinition == null ) {
			if ( multiple != null ) {
				throw new XmlParseException(getParser(), "attribute 'multiple' not allowed for root entity");
			}
			multiple = true;
		} else {
			multiple = multiple==null ? false : multiple;
		}
		// TODO maxCount should be required for multiple attributes
//		Integer maxCount = getIntegerAttribute("maxCount", multiple && defn instanceof AttributeDefinition);
		Integer maxCount = getIntegerAttribute(MAX_COUNT, false);
		definition.setMultiple(multiple);
		definition.setName(name);
		definition.setSinceVersionByName(since);
		definition.setDeprecatedVersionByName(deprecated);
		if ( minCount == null && required != null && required ) {
			definition.setMinCount(1);
		} else {
			definition.setMinCount(minCount);
		}
		definition.setMaxCount(maxCount);
		definition.setRequiredExpression(requiredIf);
		definition.setRelevantExpression(relevant);
		
		onStartDefinition();
	}
	
	protected void onStartDefinition() throws XmlParseException, XmlPullParserException, IOException {
		// no-op
	}
	
	@Override
	protected void handleAnnotation(QName qName, String value) {
		definition.setAnnotation(qName, value);
	}
	
	@Override
	protected final void handleChildTag(XmlPullReader childPR)
			throws XmlPullParserException, IOException, XmlParseException {
		
		if ( childPR instanceof NodeDefinitionPR ) {
			NodeDefinitionPR npr = (NodeDefinitionPR) childPR;
			// Store child state in case reused recursively
			EntityDefinition tmpParent = npr.parentDefinition;
			NodeDefinition tmpDefn = npr.definition;
			npr.parentDefinition = (EntityDefinition) this.definition;
			
//			System.out.println(npr.getTagName()+" parent = "+this.definition);
			super.handleChildTag(childPR);
//			System.out.println(npr.getTagName()+" parent = "+tmpParent);
			
			npr.parentDefinition = tmpParent;
			npr.definition = tmpDefn;
		} else {
			super.handleChildTag(childPR);
		}
	}
	
	protected abstract NodeDefinition createDefinition(int id);
	
	@Override
	protected final void onEndTag()
			throws XmlParseException {
		EntityDefinition parentDefinition = getParentDefinition();
		NodeDefinition definition = getDefinition();
		if ( parentDefinition == null ) {
//			System.out.println("Adding "+definition);
			Schema schema = getSchema();
			schema.addRootEntityDefinition((EntityDefinition) definition);
		} else {			
//			System.out.println("Adding "+definition+" to "+parentDefinition);
			parentDefinition.addChildDefinition(definition);
		}
	}
	
	protected class LabelPR extends LanguageSpecificTextPR {
		public LabelPR() {
			super(LABEL);
		}
		
		@Override
		protected void processText(String lang, String typeStr, String text) throws XmlParseException {
			try { 
				NodeLabel.Type type = typeStr == null ? NodeLabel.Type.INSTANCE : NodeLabel.Type.valueOf(typeStr.toUpperCase()); 
				NodeLabel label = new NodeLabel(type, lang, text);
				definition.addLabel(label);
			} catch (IllegalArgumentException e) {
				throw new XmlParseException(getParser(), "invalid type "+typeStr);
			}
		}
	}
	
	protected class PromptPR extends LanguageSpecificTextPR {
		public PromptPR() {
			super(PROMPT, true);
		}
		
		@Override
		protected void processText(String lang, String typeStr, String text) throws XmlParseException {
			try {
				Prompt.Type type = Prompt.Type.valueOf(typeStr.toUpperCase()); 
				Prompt p = new Prompt(type, lang, text);
				definition.addPrompt(p);
			} catch (IllegalArgumentException e) {
				throw new XmlParseException(getParser(), "invalid type "+typeStr);
			}
		}
	}

	protected class DescriptionPR extends LanguageSpecificTextPR {
		public DescriptionPR() {
			super(DESCRIPTION);
		}
		
		@Override
		protected void processText(LanguageSpecificText lst) {
			definition.addDescription(lst);
		}
	}
}