package org.openforis.collect.metamodel.uiconfiguration.view;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.view.ViewContext;
import org.openforis.commons.lang.Objects;

public abstract class Views {
	
	public static <O extends Object, V extends Object> List<V> fromObjects(List<O> objects, Class<V> viewType, ViewContext context) {
		List<V> views = new ArrayList<V>(objects.size());
		for (O o : objects) {
			V view = Objects.newInstance(viewType, o, context);
			views.add(view);
		}
		return views;
	}
}