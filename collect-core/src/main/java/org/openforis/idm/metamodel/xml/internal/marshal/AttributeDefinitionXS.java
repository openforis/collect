package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.CALCULATED;
import static org.openforis.idm.metamodel.xml.IdmlConstants.CHECK;
import static org.openforis.idm.metamodel.xml.IdmlConstants.COMPARE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DEFAULT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DISTANCE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.EXPR;
import static org.openforis.idm.metamodel.xml.IdmlConstants.FIELD;
import static org.openforis.idm.metamodel.xml.IdmlConstants.FIELD_LABEL;
import static org.openforis.idm.metamodel.xml.IdmlConstants.FLAG;
import static org.openforis.idm.metamodel.xml.IdmlConstants.FROM;
import static org.openforis.idm.metamodel.xml.IdmlConstants.GT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.GTE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.IF;
import static org.openforis.idm.metamodel.xml.IdmlConstants.KEY;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.LTE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.MAX;
import static org.openforis.idm.metamodel.xml.IdmlConstants.MESSAGE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.MIN;
import static org.openforis.idm.metamodel.xml.IdmlConstants.PATTERN;
import static org.openforis.idm.metamodel.xml.IdmlConstants.REFERENCED_ATTRIBUTE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.REGEX;
import static org.openforis.idm.metamodel.xml.IdmlConstants.TO;
import static org.openforis.idm.metamodel.xml.IdmlConstants.UNIQUE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.VALUE;

import java.io.IOException;
import java.util.Locale;

import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.AttributeDefinition.FieldLabel;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.ComparisonCheck;
import org.openforis.idm.metamodel.validation.CustomCheck;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.metamodel.validation.PatternCheck;
import org.openforis.idm.metamodel.validation.UniquenessCheck;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 * @param <T>
 */
abstract class AttributeDefinitionXS<T extends AttributeDefinition> extends NodeDefinitionXS<T, EntityDefinition> {

	protected AttributeDefinitionXS(String tag) {
		super(tag);
		addChildMarshallers(
				new FieldLabelXS(),
				new DefaultXS(), 
				new CheckXSDelegator()
				);
	}

	@Override
	protected void attributes(T defn) throws IOException {
		super.attributes(defn);
		attribute(KEY, defn.isKey(), false);
		attribute(CALCULATED, defn.isCalculated(), false);
		if (defn.getReferencedAttribute() != null) {
			attribute(REFERENCED_ATTRIBUTE, defn.getReferencedAttribute().getId());
		}
	}
	
	private class DefaultXS extends XmlSerializerSupport<AttributeDefault, AttributeDefinition> {
		public DefaultXS() {
			super(DEFAULT);
		}
		@Override
		protected void marshalInstances(AttributeDefinition defn) throws IOException {
			marshal(defn.getAttributeDefaults());
		}
		
		@Override
		protected void attributes(AttributeDefault d) throws IOException {
			attribute(VALUE, d.getValue());
			attribute(EXPR, d.getExpression());
			attribute(IF, d.getCondition());
		}
	}
	
	private class CheckXSDelegator extends PolymorphicXmlSerializer<Check<?>, AttributeDefinition> {
		public CheckXSDelegator() {
			setDelegate(CustomCheck.class, new CustomCheckXS());
			setDelegate(ComparisonCheck.class, new ComparisonCheckXS());
			setDelegate(UniquenessCheck.class, new UniquenessCheckXS());
			setDelegate(DistanceCheck.class, new DistanceCheckXS());
			setDelegate(PatternCheck.class, new PatternCheckXS());
		}
		@Override
		protected void marshalInstances(AttributeDefinition defn) throws IOException {
			marshal(defn.getChecks());
		}
	}
	
	private abstract class CheckXS<C extends Check<?>> extends XmlSerializerSupport<C, AttributeDefinition> {
		protected CheckXS(String tag) {
			super(tag);
			addChildMarshallers(new MessageXS());
		}
		
		@Override
		protected void attributes(C check) throws IOException {
			attribute(FLAG, check.getFlag().name().toLowerCase(Locale.ENGLISH));
			attribute(IF, check.getCondition());
		}
		
		private class MessageXS extends LanguageSpecificTextXS<Check<?>> {

			public MessageXS() {
				super(MESSAGE);
			}
			
			@Override
			protected void marshalInstances(Check<?> check) throws IOException {
				String defaultLanguage = ((SurveyMarshaller) getRootMarshaller()).getParameters().getOutputSurveyDefaultLanguage();
				marshal(check.getMessages(), defaultLanguage);
			}
		}
	}
	
	private class CustomCheckXS extends CheckXS<CustomCheck> {
		public CustomCheckXS() {
			super(CHECK);
		}
		
		@Override
		protected void attributes(CustomCheck check) throws IOException {
			super.attributes(check);
			attribute(EXPR, check.getExpression());
		}
	}
	
	private class ComparisonCheckXS extends CheckXS<ComparisonCheck> {
		public ComparisonCheckXS() {
			super(COMPARE);
		}
		
		@Override
		protected void attributes(ComparisonCheck check) throws IOException {
			super.attributes(check);
			attribute(LT, check.getLessThanExpression());
			attribute(LTE, check.getLessThanOrEqualsExpression());
			attribute(GT, check.getGreaterThanExpression());
			attribute(GTE, check.getGreaterThanOrEqualsExpression());
		}
	}
	
	private class UniquenessCheckXS extends CheckXS<UniquenessCheck> {
		public UniquenessCheckXS() {
			super(UNIQUE);
		}
		
		@Override
		protected void attributes(UniquenessCheck check) throws IOException {
			super.attributes(check);
			attribute(EXPR, check.getExpression());
		}
	}
	
	private class DistanceCheckXS extends CheckXS<DistanceCheck> {
		public DistanceCheckXS() {
			super(DISTANCE);
		}
		
		@Override
		protected void attributes(DistanceCheck check) throws IOException {
			super.attributes(check);
			attribute(MIN, check.getMinDistanceExpression());
			attribute(MAX, check.getMaxDistanceExpression());
			attribute(FROM, check.getSourcePointExpression());
			attribute(TO, check.getDestinationPointExpression());
		}
	}
	
	private class PatternCheckXS extends CheckXS<PatternCheck> {
		public PatternCheckXS() {
			super(PATTERN);
		}
		
		@Override
		protected void attributes(PatternCheck check) throws IOException {
			super.attributes(check);
			attribute(REGEX, check.getRegularExpression());
		}
	}
	
	private class FieldLabelXS extends LanguageSpecificTextXS<T> {

		public FieldLabelXS() {
			super(FIELD_LABEL);
		}
		
		@Override
		protected void attributes(LanguageSpecificText txt, boolean includeLanguage) throws IOException {
			FieldLabel label = (FieldLabel) txt;
			attribute(FIELD, label.getType());
			super.attributes(txt, includeLanguage);
		}
		
		@Override
		protected void marshalInstances(T defn) throws IOException {
			String defaultLanguage = ((SurveyMarshaller) getRootMarshaller()).getParameters().getOutputSurveyDefaultLanguage();
			marshal(defn.getFieldLabels(), defaultLanguage);
		}
	}

}
