package org.openforis.collect.utils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.Proxy;

public class Proxies {

	public static <P extends Proxy, T> List<P> fromList(List<T> objects, Class<P> proxyType) {
		if (objects == null) {
			return Collections.emptyList();
		}
		try {
			List<P> result = new ArrayList<P>(objects.size());
			for (T obj : objects) {
				Constructor<P> constructor = proxyType.getDeclaredConstructor(obj.getClass());
				result.add(constructor.newInstance(obj));
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Error creating proxies", e);
		}
	}
	
}
