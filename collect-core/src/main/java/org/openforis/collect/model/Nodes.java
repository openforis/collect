package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Collection;

import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Node;

public abstract class Nodes {
	
	private Nodes() {}
	
	public static <T extends Node<?>> Collection<CodeAttribute> filterCodeAttributes(Collection<T> nodes) {
		Collection<CodeAttribute> codeAttributes = new ArrayList<CodeAttribute>();
		for (Node<?> node : nodes) {
			if (node instanceof CodeAttribute) {
				codeAttributes.add((CodeAttribute) node);
			}
		}
		return codeAttributes;
	}
	
}
