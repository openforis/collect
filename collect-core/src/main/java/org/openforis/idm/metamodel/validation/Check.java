/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.IdmInterpretationError;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.LanguageSpecificTextMap;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.expression.BooleanExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class Check<T extends Attribute<?, ?>> implements Serializable, ValidationRule<T> {

	private static final long serialVersionUID = 1L;

	public enum Flag {
		ERROR, WARN
	}

	private Flag flag;
	private String condition;
	private LanguageSpecificTextMap messages;

	public Flag getFlag() {
		return flag == null ? Flag.ERROR : flag;
	}

	public void setFlag(Flag flag) {
		this.flag = flag;
	}
	
	public String getCondition() {
		return this.condition;
	}

	public List<LanguageSpecificText> getMessages() {
		if ( this.messages == null ) {
			return Collections.emptyList();
		} else {
			return messages.values();
		}
	}
	
	public String getMessage(String language) {
		return messages == null ? null: messages.getText(language);
	}
	
	public void setMessage(String language, String text) {
		if ( messages == null ) {
			messages = new LanguageSpecificTextMap();
		}
		messages.setText(language, text);
	}

	public void addMessage(LanguageSpecificText message) {
		if ( messages == null ) {
			messages = new LanguageSpecificTextMap();
		}
		messages.add(message);
	}

	public void removeMessage(String language) {
		if (messages != null ) {
			messages.remove(language);
		}
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public boolean evaluateCondition(Attribute<?, ?> context) {
		if (StringUtils.isBlank(condition)) {
			return true;
		} else {
			try {
				Record record = context.getRecord();
				SurveyContext surveyContext = record.getSurveyContext();
				ExpressionFactory expressionFactory = surveyContext.getExpressionFactory();
				BooleanExpression expression = expressionFactory.createBooleanExpression(condition);
				return expression.evaluate(context.getParent(), context);
			} catch (InvalidExpressionException e) {
				throw new IdmInterpretationError("Unable to evaluate condition " + condition, e);
			}
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((condition == null) ? 0 : condition.hashCode());
		result = prime * result + ((flag == null) ? 0 : flag.hashCode());
		result = prime * result + ((messages == null) ? 0 : messages.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Check<?> other = (Check<?>) obj;
		if (condition == null) {
			if (other.condition != null)
				return false;
		} else if (!condition.equals(other.condition))
			return false;
		if (flag != other.flag)
			return false;
		if (messages == null) {
			if (other.messages != null)
				return false;
		} else if (!messages.equals(other.messages))
			return false;
		return true;
	}
	
	
}
