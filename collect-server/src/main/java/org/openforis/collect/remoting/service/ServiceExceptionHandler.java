/**
 * 
 */
package org.openforis.collect.remoting.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.granite.messaging.service.DefaultServiceExceptionHandler;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceInvocationContext;

/**
 * @author S. Ricci
 *
 */
public class ServiceExceptionHandler extends DefaultServiceExceptionHandler  {

	private static final Logger LOG = LogManager.getLogger(ServiceExceptionHandler.class);
	
	private static final long serialVersionUID = 1L;

	/**
	 * Handler for service invocation exceptions.
	 * 
	 * Differently from the DefaultServiceExceptionHandler, uses the class name of the exception as fault code.
	 */
	@Override
	public ServiceException handleInvocationException(ServiceInvocationContext serviceInvocationContext, Throwable cause) {
		LOG.error("Error while communicating with collect server", cause);
		
		String code = cause.getClass().getName();
		String message = cause.getMessage();
		String detail = null;
		ServiceException serviceException = new ServiceException(code, message, detail, cause);
		return serviceException;
	}

}
