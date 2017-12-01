package org.openforis.collect.utils;

import java.beans.Expression;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.Proxy;

public class Proxies {

	public static <P extends Proxy, T> P fromObject(T obj, Class<P> proxyType) {
		if (obj == null) {
			return null;
		}
		try {
			@SuppressWarnings("unchecked")
			P proxy = (P) new Expression(proxyType, "new", new Object[] {obj}).getValue();
			return proxy;
		} catch (Exception e) {
			throw new RuntimeException("Error creating proxy", e);
		}
	}
	
	public static <P extends Proxy, T> List<P> fromList(List<T> objects, Class<P> proxyType) {
		if (objects == null) {
			return Collections.emptyList();
		}
		List<P> result = new ArrayList<P>(objects.size());
		for (T obj : objects) {
			result.add(fromObject(obj, proxyType));
		}
		return result;
	}
	
}
