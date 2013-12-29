/**
 * 
 */
package org.openforis.collect.designer.form;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.Check.Flag;

/**
 * @author S. Ricci
 *
 */
public class CheckFormObject<T extends Check<?>> extends FormObject<T> {
	
	private String flag;
	private String condition;
	private String message;
	
	@Override
	public void loadFrom(T source, String languageCode) {
		flag = source.getFlag().name();
		condition = source.getCondition();
		message = source.getMessage(languageCode);
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		dest.setFlag(Flag.valueOf(flag));
		dest.setCondition(StringUtils.trimToNull(condition));
		dest.setMessage(languageCode, StringUtils.trimToNull(message));
	}
	
	@Override
	protected void reset() {
		flag = null;
		condition = null;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
