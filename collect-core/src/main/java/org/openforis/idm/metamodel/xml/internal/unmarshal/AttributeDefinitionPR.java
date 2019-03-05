package org.openforis.idm.metamodel.xml.internal.unmarshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.CALCULATED;
import static org.openforis.idm.metamodel.xml.IdmlConstants.CHECK;
import static org.openforis.idm.metamodel.xml.IdmlConstants.COMPARE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DECIMAL_DIGITS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DEFAULT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.DISTANCE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.EQ;
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
import static org.openforis.idm.metamodel.xml.IdmlConstants.PRECISION;
import static org.openforis.idm.metamodel.xml.IdmlConstants.REFERENCED_ATTRIBUTE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.REGEX;
import static org.openforis.idm.metamodel.xml.IdmlConstants.TO;
import static org.openforis.idm.metamodel.xml.IdmlConstants.UNIQUE;
import static org.openforis.idm.metamodel.xml.IdmlConstants.UNIT;
import static org.openforis.idm.metamodel.xml.IdmlConstants.VALUE;

import java.io.IOException;
import java.util.Locale;

import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.AttributeDefinition.FieldLabel;
import org.openforis.idm.metamodel.Calculable;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.ComparisonCheck;
import org.openforis.idm.metamodel.validation.CustomCheck;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.metamodel.validation.PatternCheck;
import org.openforis.idm.metamodel.validation.UniquenessCheck;
import org.openforis.idm.metamodel.xml.XmlParseException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author G. Miceli
 * @author S. Ricci
 */
abstract class AttributeDefinitionPR extends NodeDefinitionPR {
	
	public AttributeDefinitionPR(String tagName) {
		super(tagName);
		addChildPullReaders(
				new FieldLabelPR(),
				new DefaultPR(),
				new PrecisionPR(),
				new CompareCheckPR(),
				new UniquenessCheckPR(),
				new DistanceCheckPR(), 
				new PatternCheckPR(),
				new CustomCheckPR()
		);
	}
	
	@Override
	protected void onStartDefinition() throws XmlParseException, XmlPullParserException, IOException {
		super.onStartDefinition();
		Boolean key = getBooleanAttribute(KEY, false);
		AttributeDefinition defn = (AttributeDefinition) getDefinition();
		defn.setKey(key == null ? false : key);
		
		if ( defn instanceof Calculable ) {
			boolean calculated = getBooleanAttributeWithDefault(CALCULATED, false);
			((Calculable) defn).setCalculated(calculated);
		}
		defn.setReferencedAttributeId(getIntegerAttribute(REFERENCED_ATTRIBUTE, false));
	}
	
	private class DefaultPR extends IdmlPullReader {
		private AttributeDefault attributeDefault;
		
		protected DefaultPR() {
			super(DEFAULT);
		}

		@Override
		protected void onStartTag()
				throws XmlParseException, XmlPullParserException, IOException {
			super.onStartTag();
			this.attributeDefault = new AttributeDefault();
			attributeDefault.setCondition(getAttribute(IF, false));
			attributeDefault.setExpression(getAttribute(EXPR, false));
			attributeDefault.setValue(getAttribute(VALUE, false));
		}
		
		@Override
		protected void onEndTag() throws XmlParseException {
			((AttributeDefinition) getDefinition()).addAttributeDefault(attributeDefault);
		}
	}

	private class PrecisionPR extends IdmlPullReader {
		private Precision precision;
		
		protected PrecisionPR() {
			super(PRECISION);
		}

		@Override
		protected void onStartTag()
				throws XmlParseException, XmlPullParserException, IOException {
			this.precision = new Precision();
			Boolean isDefault = getBooleanAttribute(DEFAULT, false);
			Integer decimalDigits = getIntegerAttribute(DECIMAL_DIGITS, false);
			String unitName = getAttribute(UNIT, false);
			Unit unit = getSurvey().getUnit(unitName);
			
			precision.setDecimalDigits(decimalDigits);
			precision.setUnit(unit);
			precision.setDefaultPrecision(isDefault == null ? false : isDefault);
		}
		
		@Override
		protected void onEndTag() throws XmlParseException {
			((NumericAttributeDefinition) getDefinition()).addPrecisionDefinition(precision);
		}
	}

	protected abstract class CheckPR extends IdmlPullReader {
		protected Check<?> check;
		
		protected CheckPR(String tagName) {
			super(tagName);
			addChildPullReaders(new MessagesPR());
		}
		
		@Override
		protected void onStartTag()
				throws XmlParseException, XmlPullParserException, IOException {
			XmlPullParser parser = getParser();
			this.check = createCheck(parser);
			String flagStr = getAttribute(FLAG, true);
			Check.Flag flag;
			try {
				flag = Check.Flag.valueOf(flagStr.toUpperCase(Locale.ENGLISH));
			} catch ( IllegalArgumentException e ) {
				throw new XmlParseException(getParser(), "invalid flag "+flagStr);
			}
			String condition = getAttribute(IF, false);
			check.setFlag(flag);
			check.setCondition(condition);
		}
		
		protected abstract Check<?> createCheck(XmlPullParser parser);
		
		private class MessagesPR extends LanguageSpecificTextPR {

			public MessagesPR() {
				super(MESSAGE);
			}
			
			@Override
			protected void processText(LanguageSpecificText lst) {
				check.addMessage(lst);
			}
		}
		
		@Override
		protected void onEndTag() throws XmlParseException {
			((AttributeDefinition) getDefinition()).addCheck(check);
		}
	}
	
	private class CompareCheckPR extends CheckPR {

		protected CompareCheckPR() {
			super(COMPARE);
		}
		
		@Override
		protected void onStartTag()
				throws XmlParseException, XmlPullParserException, IOException {
			super.onStartTag();
			ComparisonCheck chk = (ComparisonCheck) check;
			chk.setEqualsExpression(getAttribute(EQ, false));
			chk.setLessThanExpression(getAttribute(LT, false));
			chk.setLessThanOrEqualsExpression(getAttribute(LTE, false));
			chk.setGreaterThanExpression(getAttribute(GT, false));
			chk.setGreaterThanOrEqualsExpression(getAttribute(GTE, false));
		}

		@Override
		protected Check<?> createCheck(XmlPullParser parser) {
			return new ComparisonCheck();
		}
	}
	
	private class DistanceCheckPR extends CheckPR {

		protected DistanceCheckPR() {
			super(DISTANCE);
		}
		
		@Override
		protected void onStartTag()
				throws XmlParseException, XmlPullParserException, IOException {
			super.onStartTag();
			DistanceCheck chk = (DistanceCheck) check;
			chk.setMinDistanceExpression(getAttribute(MIN, false));
			chk.setMaxDistanceExpression(getAttribute(MAX, false));
			chk.setSourcePointExpression(getAttribute(FROM, false));
			chk.setDestinationPointExpression(getAttribute(TO, false));
		}

		@Override
		protected Check<?> createCheck(XmlPullParser parser) {
			return new DistanceCheck();
		}
	}
	
	private class UniquenessCheckPR extends CheckPR {

		protected UniquenessCheckPR() {
			super(UNIQUE);
		}
		
		@Override
		protected void onStartTag()
				throws XmlParseException, XmlPullParserException, IOException {
			super.onStartTag();
			UniquenessCheck chk = (UniquenessCheck) check;
			chk.setExpression(getAttribute(EXPR, true));
		}

		@Override
		protected Check<?> createCheck(XmlPullParser parser) {
			return new UniquenessCheck();
		}
	}
	
	private class PatternCheckPR extends CheckPR {

		protected PatternCheckPR() {
			super(PATTERN);
		}
		
		@Override
		protected void onStartTag()
				throws XmlParseException, XmlPullParserException, IOException {
			super.onStartTag();
			PatternCheck chk = (PatternCheck) check;
			chk.setRegularExpression(getAttribute(REGEX, true));
		}

		@Override
		protected Check<?> createCheck(XmlPullParser parser) {
			return new PatternCheck();
		}
	}
	
	private class CustomCheckPR extends CheckPR {

		protected CustomCheckPR() {
			super(CHECK);
		}
		
		@Override
		protected void onStartTag()
				throws XmlParseException, XmlPullParserException, IOException {
			super.onStartTag();
			CustomCheck chk = (CustomCheck) check;
			chk.setExpression(getAttribute(EXPR, true));
		}

		@Override
		protected Check<?> createCheck(XmlPullParser parser) {
			return new CustomCheck();
		}
	}
	
	protected class FieldLabelPR extends LanguageSpecificTextPR {
		
		public FieldLabelPR() {
			super(FIELD_LABEL);
		}
		
		@Override
		protected String getTypeAttribute() throws XmlParseException {
			return getAttribute(FIELD, true);
		}
		
		@Override
		protected void processText(String lang, String field, String text) throws XmlParseException {
			try { 
				FieldLabel label = new FieldLabel(field, lang, text);
				((AttributeDefinition) getDefinition()).addFieldLabel(label);
			} catch (IllegalArgumentException e) {
				throw new XmlParseException(getParser(), "invalid type " + field);
			}
		}
	}
	
	
}