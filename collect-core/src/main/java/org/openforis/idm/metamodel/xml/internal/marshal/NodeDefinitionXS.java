package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.DESCRIPTION;
import static org.openforis.idm.metamodel.xml.IdmlConstants.ID;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LABEL;
import static org.openforis.idm.metamodel.xml.IdmlConstants.MAX_COUNT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.MIN_COUNT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.MULTIPLE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.NAME;
import static org.openforis.idm.metamodel.xml.IdmlConstants.PROMPT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.RELEVANT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.REQUIRED;
import static org.openforis.idm.metamodel.xml.IdmlConstants.REQUIRED_IF;
import static org.openforis.idm.metamodel.xml.IdmlConstants.TYPE;

import java.io.IOException;
import java.util.Locale;

import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.Prompt;

/**
 * 
 * @author G. Miceli
 *
 */
public abstract class NodeDefinitionXS<T extends NodeDefinition, P> extends VersionableSurveyObjectXS<T, P> {

	protected NodeDefinitionXS(String tag) {
		super(tag);
		addChildMarshallers(
				new LabelXS(),
				new DescriptionXS(),
				new PromptXS());
	}
	
	@Override
	protected void attributes(T defn) throws IOException {
		attribute(ID, defn.getId());
		attribute(NAME, defn.getName());
		if ( defn.getParentDefinition() != null ) {
			attribute(RELEVANT, defn.getRelevantExpression());
			if ( defn.isMultiple() ) {
				attribute(MULTIPLE, true);
				attribute(MIN_COUNT, defn.getMinCountExpression());
				attribute(MAX_COUNT, defn.getMaxCountExpression());
			} else if ( defn.isAlwaysRequired() ){
				attribute(REQUIRED, true);
			} else {
				attribute(REQUIRED_IF, defn.extractRequiredExpression());
			}
		}
		super.attributes(defn);
	}
	
	private class LabelXS extends LanguageSpecificTextXS<T> {

		public LabelXS() {
			super(LABEL);
		}
		
		@Override
		protected void attributes(LanguageSpecificText txt, boolean includeLanguage) throws IOException {
			NodeLabel label = (NodeLabel) txt;
			attribute(TYPE, label.getType().name().toLowerCase(Locale.ENGLISH));
			super.attributes(txt, includeLanguage);
		}
		
		@Override
		protected void marshalInstances(T defn) throws IOException {
			String defaultLanguage = ((SurveyMarshaller) getRootMarshaller()).getParameters().getOutputSurveyDefaultLanguage();
			marshal(defn.getLabels(), defaultLanguage);
		}
	}
	
	private class DescriptionXS extends LanguageSpecificTextXS<T> {

		public DescriptionXS() {
			super(DESCRIPTION);
		}
		
		@Override
		protected void marshalInstances(T defn) throws IOException {
			String defaultLanguage = ((SurveyMarshaller) getRootMarshaller()).getParameters().getOutputSurveyDefaultLanguage();
			marshal(defn.getDescriptions(), defaultLanguage);
		}
	}
	
	private class PromptXS extends LanguageSpecificTextXS<T> {

		public PromptXS() {
			super(PROMPT);
		}
		
		@Override
		protected void attributes(LanguageSpecificText txt, boolean includeLanguage) throws IOException {
			Prompt prompt = (Prompt) txt;
			attribute(TYPE, prompt.getType().name().toLowerCase(Locale.ENGLISH));
			super.attributes(txt, includeLanguage);
		}
		
		@Override
		protected void marshalInstances(T defn) throws IOException {
			String defaultLanguage = ((SurveyMarshaller) getRootMarshaller()).getParameters().getOutputSurveyDefaultLanguage();
			marshal(defn.getPrompts(), defaultLanguage);
		}
	}
}
