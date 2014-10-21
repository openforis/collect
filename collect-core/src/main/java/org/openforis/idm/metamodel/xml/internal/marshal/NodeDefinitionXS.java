package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.*;

import java.io.IOException;

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
		attribute(RELEVANT, defn.getRelevantExpression());
		if ( defn.getParentDefinition() != null ) {
			if ( defn.isMultiple() ) {
				attribute(MULTIPLE, true);
				attribute(MIN_COUNT, defn.getMinCount());
				attribute(MAX_COUNT, defn.getMaxCount());
			} else if ( defn.getMinCount() != null && defn.getMinCount() > 0 ){
				attribute(REQUIRED, true);
			}
		}
		attribute(REQUIRED_IF, defn.getRequiredExpression());
		super.attributes(defn);
	}
	
	private class LabelXS extends LanguageSpecificTextXS<T> {

		public LabelXS() {
			super(LABEL);
		}
		
		@Override
		protected void attributes(LanguageSpecificText txt, boolean includeLanguage) throws IOException {
			NodeLabel label = (NodeLabel) txt;
			attribute(TYPE, label.getType().name().toLowerCase());
			super.attributes(txt, includeLanguage);
		}
		
		@Override
		protected void marshalInstances(T defn) throws IOException {
			String defaultLanguage = defn.getSurvey().getDefaultLanguage();
			marshal(defn.getLabels(), defaultLanguage);
		}
	}
	
	private class DescriptionXS extends LanguageSpecificTextXS<T> {

		public DescriptionXS() {
			super(DESCRIPTION);
		}
		
		@Override
		protected void marshalInstances(T defn) throws IOException {
			String defaultLanguage = defn.getSurvey().getDefaultLanguage();
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
			attribute(TYPE, prompt.getType().name().toLowerCase());
			super.attributes(txt, includeLanguage);
		}
		
		@Override
		protected void marshalInstances(T defn) throws IOException {
			String defaultLanguage = defn.getSurvey().getDefaultLanguage();
			marshal(defn.getPrompts(), defaultLanguage);
		}
	}
}
