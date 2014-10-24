package org.openforis.idm.model.expression.internal;

import org.apache.commons.jxpath.Function;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class CustomFunction implements Function {
	private final Set<String> referencedPaths;

	/**
	 * Create a custom function, optionally including paths that are referenced independent
	 * on any parameters passed to the function.
	 */
	public CustomFunction(String... referencedPaths) {
		Set<String> paths = new HashSet<String>();
		Collections.addAll(paths, referencedPaths);
		this.referencedPaths = Collections.unmodifiableSet(paths);
	}

	public Set<String> getReferencedPaths() {
		return referencedPaths;
	}
}
