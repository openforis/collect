package org.openforis.collect.designer.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.commons.lang.Objects;

public abstract class FormObjects {

	public static <T, F extends FormObject<T>> List<F> fromObjects(List<T> objects, Class<F> formObjectType) {
		return fromObjects(objects, formObjectType, null);
	}

	public static <T, F extends FormObject<T>> List<F> fromObjects(List<T> objects, Class<F> formObjectType,
			String language) {
		List<F> formObjects = new ArrayList<>(objects.size());
		for (T obj : objects) {
			F formObject = Objects.newInstance(formObjectType);
			formObject.loadFrom(obj, language);
			formObjects.add(formObject);
		}
		return formObjects;
	}

	public static <T, F extends FormObject<T>> List<T> toObjects(List<F> formObjects, Class<T> objectType) {
		return toObjects(formObjects, objectType, null);
	}

	public static <T, F extends FormObject<T>> List<T> toObjects(List<F> formObjects, Class<T> objectType,
			String language) {
		List<T> objects = new ArrayList<>(formObjects.size());
		for (F formObject : formObjects) {
			T obj = Objects.newInstance(objectType);
			formObject.saveTo(obj, language);
			objects.add(obj);
		}
		return objects;

	}
}
