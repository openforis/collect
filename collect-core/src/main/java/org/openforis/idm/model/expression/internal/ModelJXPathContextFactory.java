/**
 * 
 */
package org.openforis.idm.model.expression.internal;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;

/**
 * @author M. Togna
 * 
 */
public class ModelJXPathContextFactory extends JXPathContextFactory {

	@Override
	public JXPathContext newContext(JXPathContext parentContext, Object contextNode) {
		return ModelJXPathContext.newContext(parentContext, contextNode);
	}

}
