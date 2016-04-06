/**
 * 
 */
package org.openforis.idm.model.expression.internal;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertyPointer;
import org.openforis.idm.model.BooleanAttribute;

/**
 * @author M. Togna
 * 
 */
public class ModelLocationPath extends LocationPath {

	public ModelLocationPath(boolean absolute, Step[] steps) {
		super(absolute, steps);
	}

	@Override
	public Object computeValue(EvalContext context) {
		Object value = super.computeValue(context);

		if ( value instanceof DynamicPropertyPointer ) {
			NodePointer pointer = ((DynamicPropertyPointer) value).getValuePointer();
			Object object = pointer.getNode();
			if ( object instanceof BooleanAttribute ) {
				return pointer.getValue();
			}
		}
		
		return value;
	}

	@Override
	public Step[] getSteps() {
		return super.getSteps();
	}
}
