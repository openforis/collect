package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;

import org.openforis.commons.lang.DeepComparable;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingStepValue implements DeepComparable {
	
	public enum UpdateType {
		ATTRIBUTE('a'), FIELD('f');

		public static UpdateType fromCode(String code) {
			if (code.length() != 1) {
				throw new IllegalArgumentException("Invalid code for UpdateType: " + code);
			}
			return fromCode(code.charAt(0));
		}
		
		public static UpdateType fromCode(char code) {
			UpdateType[] values = values();
			for (UpdateType type : values) {
				if (type.code == code) {
					return type;
				}
			}
			throw new IllegalArgumentException("Invalid code for UpdateType: " + code);
		}

		private char code;

		UpdateType(char code) {
			this.code = code;
		}
		
		public char getCode() {
			return code;
		}
	}
	
	private String condition;
	private UpdateType updateType;
	private String fixExpression;
	private List<String> fieldFixExpressions = new ArrayList<String>();
	
	public DataCleansingStepValue() {
	}
	
	public String getCondition() {
		return condition;
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	public UpdateType getUpdateType() {
		return updateType;
	}
	
	public void setUpdateType(UpdateType updateType) {
		this.updateType = updateType;
	}
	
	public String getFixExpression() {
		return fixExpression;
	}
	
	public void setFixExpression(String fixExpression) {
		this.fixExpression = fixExpression;
	}
	
	public List<String> getFieldFixExpressions() {
		return fieldFixExpressions;
	}
	
	public void setFieldFixExpressions(List<String> fieldFixExpressions) {
		this.fieldFixExpressions = fieldFixExpressions;
	}
	
	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		DataCleansingStepValue other = (DataCleansingStepValue) obj;
		if (condition == null) {
			if (other.condition != null)
				return false;
		} else if (!condition.equals(other.condition))
			return false;
		if (fieldFixExpressions == null) {
			if (other.fieldFixExpressions != null)
				return false;
		} else if (!fieldFixExpressions.equals(other.fieldFixExpressions))
			return false;
		if (fixExpression == null) {
			if (other.fixExpression != null)
				return false;
		} else if (!fixExpression.equals(other.fixExpression))
			return false;
		if (updateType != other.updateType)
			return false;
		return true;
	}
	
}
