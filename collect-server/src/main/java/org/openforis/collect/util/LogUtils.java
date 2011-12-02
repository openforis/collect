/**
 * 
 */
package org.openforis.collect.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author M. Togna
 * 
 */
public class LogUtils {

	public static Log getLog(Class<?> clazz) {
		return LogFactory.getLog(clazz);
	}

	public static Log getLog(String name) {
		return LogFactory.getLog(name);
	}

}
