/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.Check.Flag;

/**
 * @author S. Ricci
 *
 */
public class CheckFormObject<T extends Check<?>> extends SurveyObjectFormObject<T> {
	
	private String flag;
	private String condition;
	
	@Override
	public void loadFrom(T source, String languageCode) {
		flag = source.getFlag().name();
		condition = source.getCondition();
	}
	
	@Override
	public void saveTo(T dest, String languageCode) {
		dest.setFlag(Flag.valueOf(flag));
		dest.setCondition(condition);
	}
	
	@Override
	protected void reset() {
		flag = null;
		condition = null;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}
	
}
